from flask import Flask, request, jsonify
import psycopg2
import os

app = Flask(__name__)

# Configuración de la base de datos
DB_HOST = os.getenv("DB_HOST", "163.172.67.59")
DB_NAME = os.getenv("DB_NAME", "human")
DB_USER = os.getenv("DB_USER", "postgres")
DB_PASS = os.getenv("DB_PASS", "Selenium123")

# Funciones auxiliares
def get_db_connection():
    return psycopg2.connect(host=DB_HOST, dbname=DB_NAME, user=DB_USER, password=DB_PASS)

@app.route("/clientes", methods=["GET"])
def listar_clientes():
    try:
        conn = get_db_connection()
        cur = conn.cursor()
        cur.execute("SELECT id, nombre FROM clientes ORDER BY id")
        clientes = cur.fetchall()
        cur.close()
        conn.close()
        return jsonify([{"id": c[0], "nombre": c[1]} for c in clientes])
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route("/entrenadores", methods=["GET"])
def listar_entrenadores():
    try:
        conn = get_db_connection()
        cur = conn.cursor()
        cur.execute("SELECT id, nombre FROM profesionales WHERE tipo = 'ENTRENAMIENTO' ORDER BY id")
        entrenadores = cur.fetchall()
        cur.close()
        conn.close()
        return jsonify([{"id": e[0], "nombre": e[1]} for e in entrenadores])
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route("/preferencias/<int:cliente_id>", methods=["GET"])
def obtener_preferencias(cliente_id):
    try:
        conn = get_db_connection()
        cur = conn.cursor()
        cur.execute("""
            SELECT entrenador_id, orden_preferencia 
            FROM preferencias_entrenador 
            WHERE cliente_id = %s 
            ORDER BY orden_preferencia
        """, (cliente_id,))
        preferencias = cur.fetchall()
        cur.close()
        conn.close()
        return jsonify([{"entrenador_id": p[0], "orden": p[1]} for p in preferencias])
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route("/reservas", methods=["POST"])
def crear_reserva():
    try:
        data = request.get_json()
        cliente_id = data["cliente_id"]
        entrenador_id = data["entrenador_id"]
        centro_id = data["centro_id"]
        fecha = data["fecha"]  # formato 'YYYY-MM-DD'
        hora_inicio = data["hora_inicio"]  # formato 'HH:MI'

        conn = get_db_connection()
        cur = conn.cursor()

        # Insertar reserva
        cur.execute("""
            INSERT INTO reservas (cliente_id, profesional_id, centro_id, fecha, hora)
            VALUES (%s, %s, %s, %s, %s)
        """, (cliente_id, entrenador_id, centro_id, fecha, hora_inicio))

        conn.commit()
        cur.close()
        conn.close()

        return jsonify({"mensaje": "Reserva creada exitosamente"}), 201
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route("/disponibilidad", methods=["GET"])
def consultar_disponibilidad():
    try:
        fecha = request.args.get("fecha")  # formato 'YYYY-MM-DD'
        centro_id = request.args.get("centro_id")

        conn = get_db_connection()
        cur = conn.cursor()

        # Buscar slots libres
        cur.execute("""
            SELECT h.hora_inicio, p.id, p.nombre, COUNT(r.id) AS ocupacion
            FROM disponibilidad h
            JOIN profesionales p ON p.id = h.profesional_id
            LEFT JOIN reservas r 
                ON r.profesional_id = p.id 
                AND r.fecha = h.fecha 
                AND r.hora = h.hora_inicio
            WHERE h.fecha = %s AND h.centro_id = %s
            GROUP BY h.hora_inicio, p.id, p.nombre
            ORDER BY h.hora_inicio
        """, (fecha, centro_id))

        slots = cur.fetchall()
        cur.close()
        conn.close()

        return jsonify([
            {"hora": s[0], "entrenador_id": s[1], "entrenador": s[2], "ocupacion": s[3]} for s in slots
        ])
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route("/reservas/estructura", methods=["GET"])
def estructura_reservas():
    try:
        conn = get_db_connection()
        cur = conn.cursor()
        cur.execute("""
            SELECT column_name, data_type
            FROM information_schema.columns
            WHERE table_name = 'reservas'
            ORDER BY ordinal_position
        """)
        columnas = cur.fetchall()
        cur.close()
        conn.close()
        return jsonify([{"columna": c[0], "tipo": c[1]} for c in columnas])
    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == "__main__":
    app.run(host="0.0.0.0", debug=True, port=5005)
