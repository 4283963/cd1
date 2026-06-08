import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from flask import Flask, request, jsonify
from flask_cors import CORS
from config import Config
from image_manager import ImageManager
from backend_client import BackendClient

app = Flask(__name__)
CORS(app)

image_manager = ImageManager(Config.IMAGE_STORAGE_PATH)
backend_client = BackendClient(Config.BACKEND_URL, Config.FEEDER_ID)


@app.route('/health', methods=['GET'])
def health_check():
    return jsonify({
        "status": "ok",
        "feederId": Config.FEEDER_ID,
        "service": "cat-feeder-iot"
    })


@app.route('/api/sensor', methods=['POST'])
def receive_sensor_data():
    data = request.get_json()
    if not data:
        return jsonify({"error": "无效的传感器数据"}), 400
    
    result = backend_client.report_sensor_data(data)
    
    return jsonify({
        "success": True,
        "message": "传感器数据已接收",
        "data": data,
        "forwarded": result is not None
    })


@app.route('/api/capture', methods=['POST'])
def receive_capture():
    if 'image' not in request.files:
        return jsonify({"error": "未找到图片文件"}), 400
    
    image_file = request.files['image']
    if image_file.filename == '':
        return jsonify({"error": "文件名为空"}), 400
    
    feeder_id = request.form.get('feederId', Config.FEEDER_ID)
    cat_features = request.form.get('catFeatures')
    
    image_data = image_file.read()
    
    if len(image_data) > Config.MAX_IMAGE_SIZE:
        return jsonify({"error": "图片大小超过限制"}), 413
    
    filename, filepath = image_manager.save_image(image_data, feeder_id)
    image_manager.compress_image(filepath)
    
    cat_features_dict = None
    if cat_features:
        import json
        try:
            cat_features_dict = json.loads(cat_features)
        except json.JSONDecodeError:
            pass
    
    result = backend_client.report_cat_capture(filename, filepath, cat_features_dict)
    
    return jsonify({
        "success": True,
        "message": "图片已接收并保存",
        "filename": filename,
        "forwarded": result is not None,
        "catRecognized": result.get('catRecognized', False) if result else False
    })


@app.route('/api/feeding', methods=['POST'])
def record_feeding():
    data = request.get_json()
    if not data or 'amount' not in data:
        return jsonify({"error": "缺少喂食量参数"}), 400
    
    amount = data.get('amount')
    cat_id = data.get('catId')
    
    result = backend_client.report_feeding_event(amount, cat_id)
    
    return jsonify({
        "success": True,
        "message": "喂食事件已记录",
        "amount": amount,
        "catId": cat_id,
        "forwarded": result is not None
    })


@app.route('/api/heartbeat', methods=['POST'])
def heartbeat():
    data = request.get_json() or {}
    
    result = backend_client.heart_beat(data)
    
    return jsonify({
        "success": True,
        "message": "心跳已接收",
        "feederId": Config.FEEDER_ID,
        "forwarded": result is not None
    })


@app.route('/api/image/<filename>', methods=['GET'])
def get_image(filename):
    filepath = image_manager.get_image_path(filename)
    if os.path.exists(filepath):
        from flask import send_file
        return send_file(filepath, mimetype='image/jpeg')
    return jsonify({"error": "图片不存在"}), 404


if __name__ == '__main__':
    print(f"猫咪喂养机IoT服务启动中...")
    print(f"喂养机ID: {Config.FEEDER_ID}")
    print(f"监听地址: {Config.HOST}:{Config.PORT}")
    print(f"后端服务: {Config.BACKEND_URL}")
    app.run(host=Config.HOST, port=Config.PORT, debug=False)
