import requests
import json
from datetime import datetime


class BackendClient:
    def __init__(self, base_url, feeder_id):
        self.base_url = base_url
        self.feeder_id = feeder_id

    def report_sensor_data(self, sensor_data):
        url = f"{self.base_url}/iot/sensor"
        payload = {
            "feederId": self.feeder_id,
            "timestamp": datetime.now().isoformat(),
            "infraredTriggered": sensor_data.get('infrared_triggered', False),
            "foodLevel": sensor_data.get('food_level'),
            "waterLevel": sensor_data.get('water_level'),
            "temperature": sensor_data.get('temperature'),
            "batteryLevel": sensor_data.get('battery_level')
        }
        try:
            response = requests.post(url, json=payload, timeout=10)
            response.raise_for_status()
            return response.json()
        except requests.RequestException as e:
            print(f"上报传感器数据失败: {e}")
            return None

    def report_cat_capture(self, image_filename, image_path, cat_features=None):
        url = f"{self.base_url}/iot/capture"
        try:
            with open(image_path, 'rb') as f:
                files = {'image': (image_filename, f, 'image/jpeg')}
                data = {
                    'feederId': self.feeder_id,
                    'timestamp': datetime.now().isoformat()
                }
                if cat_features:
                    data['catFeatures'] = json.dumps(cat_features)
                
                response = requests.post(url, files=files, data=data, timeout=30)
                response.raise_for_status()
                return response.json()
        except requests.RequestException as e:
            print(f"上报抓拍图片失败: {e}")
            return None

    def report_feeding_event(self, amount, cat_id=None):
        url = f"{self.base_url}/iot/feeding"
        payload = {
            "feederId": self.feeder_id,
            "timestamp": datetime.now().isoformat(),
            "amount": amount,
            "catId": cat_id
        }
        try:
            response = requests.post(url, json=payload, timeout=10)
            response.raise_for_status()
            return response.json()
        except requests.RequestException as e:
            print(f"上报喂食事件失败: {e}")
            return None

    def heart_beat(self, status_info):
        url = f"{self.base_url}/iot/heartbeat"
        payload = {
            "feederId": self.feeder_id,
            "timestamp": datetime.now().isoformat(),
            "status": status_info.get('status', 'online'),
            "foodLevel": status_info.get('food_level'),
            "waterLevel": status_info.get('water_level'),
            "batteryLevel": status_info.get('battery_level')
        }
        try:
            response = requests.post(url, json=payload, timeout=10)
            response.raise_for_status()
            return response.json()
        except requests.RequestException as e:
            print(f"心跳上报失败: {e}")
            return None
