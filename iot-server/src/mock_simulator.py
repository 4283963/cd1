import os
import sys
import time
import random
import requests
from datetime import datetime

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from config import Config


def generate_mock_sensor_data():
    return {
        "infrared_triggered": random.choice([True, False]),
        "food_level": random.randint(10, 100),
        "water_level": random.randint(10, 100),
        "temperature": round(random.uniform(15, 35), 1),
        "battery_level": random.randint(50, 100)
    }


def generate_mock_cat_features():
    fur_colors = ['orange', 'black', 'white', 'gray', 'calico', 'tabby']
    fur_patterns = ['solid', 'tabby', 'tricolor', 'striped']
    body_types = ['slim', 'normal', 'fat']
    eye_colors = ['yellow', 'green', 'blue', 'brown']

    return {
        "furColor": random.choice(fur_colors),
        "furPattern": random.choice(fur_patterns),
        "bodyType": random.choice(body_types),
        "eyeColor": random.choice(eye_colors),
        "gender": random.choice(['male', 'female'])
    }


def simulate_sensor_data():
    print("开始模拟传感器数据上报...")
    url = f"http://localhost:{Config.PORT}/api/sensor"

    try:
        while True:
            data = generate_mock_sensor_data()
            data['feederId'] = Config.FEEDER_ID
            data['timestamp'] = datetime.now().isoformat()

            try:
                response = requests.post(url, json=data, timeout=5)
                print(f"[{datetime.now().strftime('%H:%M:%S')}] 传感器数据上报: {'成功' if response.ok else '失败'} - 粮草:{data['food_level']}% 水量:{data['water_level']}%")
            except requests.RequestException as e:
                print(f"[{datetime.now().strftime('%H:%M:%S')}] 上报失败: {e}")

            time.sleep(30)

    except KeyboardInterrupt:
        print("\n模拟结束")


def simulate_cat_capture():
    print("模拟猫咪抓拍...")
    from PIL import Image
    import io
    import json

    url = f"http://localhost:{Config.PORT}/api/capture"

    features = generate_mock_cat_features()

    img = Image.new('RGB', (640, 480), color=(200, 200, 180))

    img_byte_arr = io.BytesIO()
    img.save(img_byte_arr, format='JPEG')
    img_byte_arr = img_byte_arr.getvalue()

    try:
        files = {'image': ('capture.jpg', img_byte_arr, 'image/jpeg')}
        data = {
            'feederId': Config.FEEDER_ID,
            'timestamp': datetime.now().isoformat(),
            'catFeatures': json.dumps(features)
        }

        response = requests.post(url, files=files, data=data, timeout=10)
        result = response.json()
        print(f"抓拍上报: {'成功' if result.get('success') else '失败'}")
        print(f"猫咪识别: {'是' if result.get('catRecognized') else '否'}")
        print(f"是否新猫: {'是' if result.get('isNewCat') else '否'}")
        if result.get('catName'):
            print(f"猫咪名字: {result['catName']}")
        if result.get('catId'):
            print(f"猫咪ID: {result['catId']}")

    except requests.RequestException as e:
        print(f"上报失败: {e}")


def show_help():
    print("猫咪喂养机IoT模拟器")
    print("用法: python mock_simulator.py [命令]")
    print("")
    print("命令:")
    print("  sensor    - 持续模拟传感器数据上报")
    print("  capture   - 模拟一次猫咪抓拍")
    print("  feeding   - 模拟一次喂食事件")
    print("  heartbeat - 发送一次心跳")
    print("  all       - 模拟完整流程（传感器+抓拍）")
    print("  help      - 显示帮助信息")


def simulate_feeding():
    print("模拟喂食事件...")
    url = f"http://localhost:{Config.PORT}/api/feeding"

    data = {
        "feederId": Config.FEEDER_ID,
        "timestamp": datetime.now().isoformat(),
        "amount": random.randint(20, 50)
    }

    try:
        response = requests.post(url, json=data, timeout=5)
        result = response.json()
        print(f"喂食事件上报: {'成功' if result.get('success') else '失败'}")
        print(f"喂食量: {data['amount']}g")
    except requests.RequestException as e:
        print(f"上报失败: {e}")


def simulate_heartbeat():
    print("发送心跳...")
    url = f"http://localhost:{Config.PORT}/api/heartbeat"

    data = generate_mock_sensor_data()
    data['status'] = 'online'

    try:
        response = requests.post(url, json=data, timeout=5)
        result = response.json()
        print(f"心跳上报: {'成功' if result.get('success') else '失败'}")
    except requests.RequestException as e:
        print(f"上报失败: {e}")


if __name__ == '__main__':
    command = sys.argv[1] if len(sys.argv) > 1 else 'help'

    if command == 'sensor':
        simulate_sensor_data()
    elif command == 'capture':
        simulate_cat_capture()
    elif command == 'feeding':
        simulate_feeding()
    elif command == 'heartbeat':
        simulate_heartbeat()
    elif command == 'all':
        print("模拟完整流程...")
        simulate_heartbeat()
        time.sleep(1)
        simulate_sensor_data()
    elif command == 'help':
        show_help()
    else:
        print(f"未知命令: {command}")
        show_help()
