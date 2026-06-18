import os
from PIL import Image

def batch_convert_folder(folder_path, new_path):
    if not os.path.exists(folder_path):
        print("The specified directory does not exist.")
        return

    for filename in os.listdir(folder_path):
        if filename.lower().endswith(".webp"):
            input_path = os.path.join(folder_path, filename)
            
            output_filename = os.path.splitext(filename)[0] + ".png"
            output_path = os.path.join(new_path, output_filename)
            
            try:
                with Image.open(input_path) as img:
                    img.save(output_path, "PNG")
                print(f"Converted: {filename} -> {output_filename}")
            except Exception as e:
                print(f"Failed to convert {filename}: {e}")

batch_convert_folder(".\\grabber\\Images", ".\\grabber\\PNGs")
