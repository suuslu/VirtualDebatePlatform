from flask import Flask, request, jsonify
from flask_sqlalchemy import SQLAlchemy
from flask_cors import CORS
from flask_jwt_extended import JWTManager, create_access_token, jwt_required, get_jwt_identity
from sqlalchemy import text
from werkzeug.utils import secure_filename
from dotenv import load_dotenv
from openai import OpenAI
import datetime
import os
import json
import re
import subprocess

app = Flask(__name__)
from faster_whisper import WhisperModel
model = WhisperModel("tiny", compute_type="float32")  # Daha hızlı model
load_dotenv()

print("\u2705 OpenRouter Key Present:", os.getenv("OPENROUTER_API_KEY") is not None)

app.config['SQLALCHEMY_DATABASE_URI'] = 'mysql+pymysql://root@localhost/virtual_debate_db'
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
app.config['JWT_SECRET_KEY'] = 'your-secret-key'

UPLOAD_FOLDER = os.path.join('static', 'uploads')
os.makedirs(UPLOAD_FOLDER, exist_ok=True)
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER
app.static_folder = 'static'

db = SQLAlchemy(app)
jwt = JWTManager(app)
CORS(app)

# Models
debate_user_assoc = db.Table(
    'debate_user_assoc',
    db.Column('debate_id', db.Integer, db.ForeignKey('debate.id')),
    db.Column('user_id', db.Integer, db.ForeignKey('user.id'))
)

class User(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    email = db.Column(db.String(120), unique=True, nullable=False)
    username = db.Column(db.String(80), nullable=False)
    password = db.Column(db.String(80), nullable=False)

class Debate(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    topic = db.Column(db.String(200), nullable=False)
    time_limit = db.Column(db.Integer, nullable=False)
    participants = db.relationship('User', secondary=debate_user_assoc, backref='debates')
    created_at = db.Column(db.DateTime, default=datetime.datetime.utcnow)

class Feedback(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(80), nullable=False)
    debate_id = db.Column(db.Integer, nullable=False)
    logic = db.Column(db.Text, nullable=False)
    clarity = db.Column(db.Text, nullable=False)
    impact = db.Column(db.Text, nullable=False)
    score = db.Column(db.Integer, nullable=False)
    created_at = db.Column(db.DateTime, default=datetime.datetime.utcnow)

@app.route('/register', methods=['POST'])
def register():
    data = request.get_json()
    email = data.get('email')
    username = data.get('username')
    password = data.get('password')

    if not email or not username or not password:
        return jsonify(message="T\u00fcm alanlar doldurulmal\u0131d\u0131r"), 400

    if User.query.filter_by(email=email).first():
        return jsonify(message="Bu email zaten kay\u0131tl\u0131"), 409

    new_user = User(email=email, username=username, password=password)
    db.session.add(new_user)
    db.session.commit()
    return jsonify(message="Kay\u0131t ba\u015far\u0131l\u0131"), 200

@app.route('/login', methods=['POST'])
def login():
    data = request.get_json()
    email = data.get('email')
    password = data.get('password')

    user = User.query.filter_by(email=email).first()
    if user and user.password == password:
        token = create_access_token(identity=email, expires_delta=datetime.timedelta(hours=1))
        return jsonify(token=token, username=user.username), 200
    return jsonify(message="Email veya \u015fifre yanl\u0131\u015f"), 401

@app.route('/whoami', methods=['GET'])
@jwt_required()
def whoami():
    user = get_jwt_identity()
    return jsonify(message=f"Giri\u015f yapm\u0131\u015f kullan\u0131c\u0131: {user}")

@app.route('/create_debate', methods=['POST'])
@jwt_required()
def create_debate():
    data = request.get_json()
    topic = data.get('topic')
    time_limit = data.get('time_limit')
    participants = data.get('participants')

    if not topic or not time_limit or not participants:
        return jsonify(message="Eksik veri"), 400

    participant_names = participants if isinstance(participants, list) else participants.split(",")
    users = User.query.filter(User.username.in_(participant_names)).all()

    if len(users) != len(participant_names):
        return jsonify(message="Baz\u0131 kullan\u0131c\u0131lar bulunamad\u0131"), 400

    new_debate = Debate(topic=topic, time_limit=int(time_limit), participants=users)
    db.session.add(new_debate)
    db.session.commit()
    return jsonify(message="M\u00fcnazara olu\u015fturuldu", debate_id=new_debate.id), 200

@app.route('/users', methods=['GET'])
@jwt_required()
def get_users():
    current_email = get_jwt_identity()
    current_user = User.query.filter_by(email=current_email).first()
    users = User.query.filter(User.id != current_user.id).all()
    return jsonify([{ "id": u.id, "email": u.email, "username": u.username } for u in users])

@app.route("/get_speakers", methods=["POST"])
def get_speakers():
    try:
        data = request.get_json()
        debate_id = int(data.get("debateId", -1))
        if debate_id == -1:
            return jsonify({"message": "Invalid debate ID"}), 400

        result = db.session.execute(text("""
            SELECT user.username
            FROM debate_user_assoc
            JOIN user ON user.id = debate_user_assoc.user_id
            WHERE debate_user_assoc.debate_id = :debate_id
        """), {"debate_id": debate_id})

        speakers = [row.username for row in result]
        return jsonify({"speakers": speakers}), 200
    except Exception as e:
        return jsonify({"message": str(e)}), 500

@app.route("/submit_rating", methods=["POST"])
def submit_rating():
    try:
        data = request.form
        debate_id = int(data.get("debateId", -1))
        speaker = data.get("speaker")
        score = float(data.get("score", 0))

        stmt = text("""
            INSERT INTO speaker_scores (debate_id, username, score)
            VALUES (:debate_id, :speaker, :score)
            ON DUPLICATE KEY UPDATE score = score + VALUES(score)
        """)
        db.session.execute(stmt, {
            "debate_id": debate_id,
            "speaker": speaker,
            "score": score
        })
        db.session.commit()
        return jsonify({"message": "Rating submitted"}), 200
    except Exception as e:
        return jsonify({"message": f"Server error: {str(e)}"}), 500

@app.route("/submit_rating_json", methods=["POST"])
def submit_rating_json():
    try:
        data = request.get_json()
        debate_id = int(data.get("debateId", -1))
        speaker = data.get("speaker")
        score = float(data.get("score", 0))

        stmt = text("""
            INSERT INTO speaker_scores (debate_id, username, score)
            VALUES (:debate_id, :speaker, :score)
            ON DUPLICATE KEY UPDATE score = score + VALUES(score)
        """)
        db.session.execute(stmt, {
            "debate_id": debate_id,
            "speaker": speaker,
            "score": score
        })
        db.session.commit()
        return jsonify({"message": "Rating submitted (JSON)"}), 200
    except Exception as e:
        return jsonify({"message": f"Server error: {str(e)}"}), 500

@app.route('/analyze_audio', methods=['POST'])
def analyze_audio():
    if 'audio' not in request.files:
        return jsonify(message="Ses dosyas\u0131 g\u00f6nderilmedi"), 400

    audio_file = request.files['audio']
    if audio_file.filename == '':
        return jsonify(message="Bo\u015f ses dosyas\u0131 ad\u0131 al\u0131nd\u0131"), 400

    username = request.form.get('username', 'unknown_user')
    debate_id_raw = request.form.get('debateId', '-1')

    try:
        debate_id = int(debate_id_raw)
    except ValueError:
        return jsonify(message="Invalid debate ID"), 400

    safe_user = re.sub(r'\W+', '_', username)
    safe_debate = re.sub(r'\W+', '_', str(debate_id))
    timestamp = int(datetime.datetime.now().timestamp())
    filename = secure_filename(f"{safe_user}_{timestamp}.m4a")

    user_folder = os.path.join(app.config['UPLOAD_FOLDER'], safe_debate)
    os.makedirs(user_folder, exist_ok=True)
    file_path = os.path.join(user_folder, filename)
    audio_file.save(file_path)

    try:
        subprocess.check_output(["ffprobe", "-v", "error", "-show_entries",
                                 "format=duration", "-of",
                                 "default=noprint_wrappers=1:nokey=1", file_path])
    except Exception as e:
        print("[ffprobe error]", str(e))

    segments, _ = model.transcribe(file_path)
    transcript = " ".join([s.text for s in segments])
    if not transcript.strip():
        return jsonify({"message": "Transcript bo\u015f, analiz yap\u0131lmad\u0131"}), 400
    print("[SEGMENTS]", [s.text for s in segments])
    print("[TRANSCRIPT]", repr(transcript))

    messages = [
    {
        "role": "system",
        "content": (
            "You are an AI debate judge. Analyze the transcript provided by the user. "
            "Only return a valid JSON object with the following keys: logic, clarity, impact, and score. "
            "Example:\n"
            "{\"logic\": \"...\", \"clarity\": \"...\", \"impact\": \"...\", \"score\": 7}\n"
            "Do not return any explanations, markdown, or extra text. Respond with only the JSON."
        )
    },
    {
        "role": "user",
        "content": transcript
    }
]

    try:
        client = OpenAI(base_url="https://openrouter.ai/api/v1", api_key=os.getenv("OPENROUTER_API_KEY"))
        response = client.chat.completions.create(model="openai/gpt-3.5-turbo", messages=messages)
        reply = response.choices[0].message.content
        print("[RAW_AI_REPLY]", repr(reply))
        cleaned = re.sub(r"^```json|```$", "", reply.strip(), flags=re.IGNORECASE).strip()
        feedback = json.loads(cleaned)

        new_feedback = Feedback(
            username=username,
            debate_id=debate_id,
            logic=feedback.get("logic", ""),
            clarity=feedback.get("clarity", ""),
            impact=feedback.get("impact", ""),
            score=int(round(float(feedback.get("score", 0))))
        )
        db.session.add(new_feedback)
        db.session.commit()
    except Exception as e:
        print("[AI ERROR]", str(e))
        feedback = {
            "logic": "Error evaluating logic.",
            "clarity": "Error evaluating clarity.",
            "impact": "Error evaluating impact.",
            "score": 0
        }

    return jsonify({"message": "Analiz tamamland\u0131", "path": file_path, "feedback": feedback}), 200

@app.route('/get_rankings', methods=['GET'])
def get_rankings():
    try:
        result = db.session.execute(text("""
            SELECT username, SUM(score) AS total_score
            FROM speaker_scores
            GROUP BY username
            ORDER BY total_score DESC
        """))
        rankings = [{"username": row.username, "score": int(row.total_score)} for row in result]
        return jsonify(rankings), 200
    except Exception as e:
        return jsonify({"message": f"Ranking fetch error: {str(e)}"}), 500

@app.route('/user_past_debates', methods=['POST'])
def user_past_debates():
    data = request.get_json()
    username = data.get("username")

    if not username:
        return jsonify({"message": "Username missing"}), 400

    result = db.session.execute(text("""
        SELECT d.topic
        FROM debate d
        JOIN debate_user_assoc dua ON d.id = dua.debate_id
        JOIN user u ON u.id = dua.user_id
        WHERE u.username = :username
        ORDER BY d.created_at DESC
        LIMIT 3
    """), {"username": username})

    debates = [row.topic for row in result]
    return jsonify({"past_debates": debates}), 200

if __name__ == "__main__":
    print("Flask app starting")
    with app.app_context():
        db.create_all()
    app.run(host="0.0.0.0", port=5001, debug=True)
