from flask import Flask, request, jsonify
from flask_sqlalchemy import SQLAlchemy
from flask_cors import CORS
from flask_jwt_extended import JWTManager, create_access_token, jwt_required, get_jwt_identity
import datetime

app = Flask(__name__)

# Flask konfigürasyonu ve SQLAlchemy yapılandırması
app.config['SQLALCHEMY_DATABASE_URI'] = 'mysql+pymysql://root@localhost/virtual_debate_db'
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
app.config['JWT_SECRET_KEY'] = 'your-secret-key'

db = SQLAlchemy(app)

# Debate-User many-to-many relationship table
debate_user_assoc = db.Table(
    'debate_user_assoc',
    db.Column('debate_id', db.Integer, db.ForeignKey('debate.id')),
    db.Column('user_id', db.Integer, db.ForeignKey('user.id'))
)

jwt = JWTManager(app)
CORS(app)

# Kullanıcı modeli
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

# Register endpoint'i
@app.route('/register', methods=['POST'])
def register():
    data = request.get_json()
    email = data.get('email')
    username = data.get('username')
    password = data.get('password')

    if not email or not username or not password:
        return jsonify(message="Tüm alanlar doldurulmalıdır"), 400

    existing_user = User.query.filter_by(email=email).first()
    if existing_user:
        return jsonify(message="Bu email zaten kayıtlı"), 409

    new_user = User(email=email, username=username, password=password)
    db.session.add(new_user)
    db.session.commit()

    return jsonify(message="Kayıt başarılı"), 200

# Login endpoint'i
@app.route('/login', methods=['POST'])
def login():
    data = request.get_json()
    email = data.get('email')
    password = data.get('password')

    if not email or not password:
        return jsonify(message="Email ve şifre boş olamaz"), 400

    user = User.query.filter_by(email=email).first()
    if user and user.password == password:
        token = create_access_token(identity=email, expires_delta=datetime.timedelta(hours=1))
        return jsonify(token=token, username=user.username), 200

    return jsonify(message="Email veya şifre yanlış"), 401

# JWT korumalı endpoint örneği
@app.route('/whoami', methods=['GET'])
@jwt_required()
def whoami():
    user = get_jwt_identity()
    return jsonify(message=f"Giriş yapmış kullanıcı: {user}")

# Debate oluşturma
@app.route('/create_debate', methods=['POST'])
@jwt_required()
def create_debate():
    data = request.get_json()
    topic = data.get('topic')
    time_limit = data.get('time_limit')
    participants = data.get('participants')

    if not topic or not time_limit or not participants:
        return jsonify(message="Tüm alanlar doldurulmalıdır"), 400

    if not str(time_limit).isdigit() or not (1 <= int(time_limit) <= 3):
        return jsonify(message="Zaman limiti 1 ile 3 dakika arasında olmalıdır"), 400

    participant_names = [name.strip() for name in participants.split(',')]
    users = User.query.filter(User.username.in_(participant_names)).all()

    if len(users) != len(participant_names):
        return jsonify(message="Bir veya daha fazla kullanıcı bulunamadı"), 400

    new_debate = Debate(
        topic=topic,
        time_limit=int(time_limit),
        participants=users
    )
    db.session.add(new_debate)
    db.session.commit()

    return jsonify(message="Münazara başarıyla oluşturuldu", debate_id=new_debate.id), 200

@app.route('/users', methods=['GET'])
@jwt_required()
def get_users():
    current_user_email = get_jwt_identity()
    current_user = User.query.filter_by(email=current_user_email).first()

    users = User.query.filter(User.id != current_user.id).all()

    return jsonify([
        {
            "id": user.id,
            "email": user.email,
            "username": user.username
        } for user in users
    ])

# Uygulama başlatma
if __name__ == "__main__":
    with app.app_context():
        db.create_all()  # Veritabanı tablolarını oluştur
    app.run(host="0.0.0.0", port=5001)