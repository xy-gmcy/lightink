import os
from kindleunpack import kindleunpack

def unpack(input_path, output_dir):
    if not os.path.exists(output_dir):
        os.makedirs(output_dir)

    kindleunpack.unpackBook(input_path, output_dir)
    return output_dir