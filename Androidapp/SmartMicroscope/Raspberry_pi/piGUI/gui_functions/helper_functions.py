import numpy as np

def norm(array):
    norm_array = (array - array.min())/(array.max() - array.min())
    return norm_array

def to_8_bit(array):
    array_8_bit = np.round(norm(array)*(2**8 - 1)).astype(np.uint8)
    return array_8_bit


def get_int_from_string(string,loc,direction = 1):
    count = 0
    while True:
        try:
            if direction == 1:
                int(string[loc:loc + count +1])
                if loc +count > len(string):
                    break
            elif direction == -1:
                if loc == -1:
                    int(string[loc-count:])
                else:
                    int(string[loc-count:loc+1])
            else:
                raise ValueError('Direction argument must be 1 or -1')
            count += 1
        except Exception:
            break

    if direction == 1:
        return int(string[loc:loc + count])
    elif direction == -1:
        if loc == -1:
            return int(string[loc-count+1:])
        else:
            return int(string[loc-count+1:loc+1])
