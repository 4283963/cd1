import os
from dotenv import load_dotenv

load_dotenv()

class Config:
    PORT = int(os.getenv('PORT', 5000))
    HOST = os.getenv('HOST', '0.0.0.0')
    BACKEND_URL = os.getenv('BACKEND_URL', 'http://localhost:8080/api')
    FEEDER_ID = os.getenv('FEEDER_ID', 'feeder-001')
    IMAGE_STORAGE_PATH = os.getenv('IMAGE_STORAGE_PATH', './uploads')
    MAX_IMAGE_SIZE = int(os.getenv('MAX_IMAGE_SIZE', 10 * 1024 * 1024))
    SENSOR_DATA_INTERVAL = int(os.getenv('SENSOR_DATA_INTERVAL', 60))
