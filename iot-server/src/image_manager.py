import os
import uuid
from datetime import datetime
from PIL import Image


class ImageManager:
    def __init__(self, storage_path):
        self.storage_path = storage_path
        os.makedirs(storage_path, exist_ok=True)
        os.makedirs(os.path.join(storage_path, 'captures'), exist_ok=True)

    def save_image(self, image_data, feeder_id):
        timestamp = datetime.now().strftime('%Y%m%d_%H%M%S')
        unique_id = str(uuid.uuid4())[:8]
        filename = f"{feeder_id}_{timestamp}_{unique_id}.jpg"
        filepath = os.path.join(self.storage_path, 'captures', filename)
        
        if isinstance(image_data, bytes):
            with open(filepath, 'wb') as f:
                f.write(image_data)
        else:
            image_data.save(filepath, 'JPEG', quality=85)
        
        return filename, filepath

    def get_image_path(self, filename):
        return os.path.join(self.storage_path, 'captures', filename)

    def compress_image(self, image_path, quality=70):
        try:
            img = Image.open(image_path)
            if img.mode != 'RGB':
                img = img.convert('RGB')
            img.save(image_path, 'JPEG', quality=quality, optimize=True)
            return True
        except Exception:
            return False
