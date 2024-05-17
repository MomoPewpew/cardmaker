import os

def get_font_info(fonts_dir, output_dir, output_filename):
  """
  This function iterates over all files in the specified "fonts" folder and creates a list of dictionaries containing font filename and family name (if available 
  from the filename). Generates a Kotlin data class file with the information in the specified output directory.

  Args:
      fonts_dir: Path to the directory containing the font files (relative to the script's location).
      output_dir: Path to the directory where the Kotlin data class file should be saved (relative to the script's location).
      output_filename: Name of the output Kotlin data class file.
  """
  # Get the script's directory
  script_dir = os.getcwd()  # Get the current working directory

  # Construct full paths based on relative paths and script directory
  fonts_dir = os.path.join(script_dir, fonts_dir)
  output_dir = os.path.join(script_dir, output_dir)
  output_file_path = os.path.join(output_dir, output_filename)

  font_data = []
  for filename in os.listdir(fonts_dir):
    # Extract font family (heuristic based on filename format)
    font_family = os.path.splitext(filename)[0].split("-")[0]  # Assuming family before hyphen
    
    # Check if file extension is a common font format (TTF, OTF)
    if filename.endswith(".ttf") or filename.endswith(".otf"):
      font_data.append({
          "filename": filename,
          "family": font_family  # Might need adjustment based on your naming convention
      })

  # Generate Kotlin data class content
  data_class_content = f"""package com.momo.cardmaker\n
data class FontInfo(val filename: String, val family: String)

val fontList = listOf(\n"""

  for font in font_data:
    data_class_content += f'    FontInfo("fonts/{font["filename"]}", "{font["family"]}"),\n'

  data_class_content = data_class_content[:-1] + "\n)"

  # Write the Kotlin data class content to the file
  with open(output_file_path, "w") as output_file:
    output_file.write(data_class_content)

  print(f"Font information written to Kotlin data class file: {output_file_path}")

# Define relative paths for your project structure
fonts_dir = "composeApp/src/commonMain/resources/fonts"
output_dir = "composeApp/src/commonMain/kotlin/com/momo/cardmaker"
output_filename = "FontInfo.kt"  # You can change this

# Generate the Kotlin data class file
get_font_info(fonts_dir, output_dir, output_filename)
