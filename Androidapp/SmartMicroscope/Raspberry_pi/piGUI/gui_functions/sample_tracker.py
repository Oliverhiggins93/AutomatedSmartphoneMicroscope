'''
A class to deal with tracking sample number

Filesystem

home/pi/lensfree/data/
├── 00001/
│   ├── notes.txt
│   ├── microscope/
│   │    ├── sample_00001_microscope_image_1.tif
│   │    ├── sample_00001_microscope_metadata_1.npy
│   │    ├── sample_00001_microscope_egg_locs_1.txt
│   │    ├── sample_00001_microscope_image_2.tif
│   │    ├── sample_00001_microscope_metadata_2.npy
│   │    ├── sample_00001_microscope_egg_locs_2.txt
│   │    ├── sample_00001_microscope_image_3.tif
│   │    └── sample_00001_microscope_metadata_3.npy
│   └── lensfree/
│        ├── sample_00001_lensfree_image_1.tif
│        ├── sample_00001_lensfree_metadata_1.npy
│        ├── sample_00001_lensfree_image_2.tif
│        ├── sample_00001_lensfree_metadata_2.npy
│        ├── sample_00001_lensfree_image_3.tif
│        └── sample_00001_lensfree_metadata_3.npy
│
├── 00002/...


'''

from pathlib import Path
from . import helper_functions as hf
#import tifffile
import numpy as np

class SampleTracker():
    def __init__(self,save_directory,sample_number):
        self.sample_number = sample_number
        self.parent_save_directory = Path(save_directory)

        self.sample_savedir_microscope,first_microscope_instantiation = self._generate_save_directory('microscope')
        self.sample_savedir_lensfree,first_lensfree_instantiation = self._generate_save_directory('reverselens')

        if not first_microscope_instantiation:
            self.microscope_image_number = self._get_latest_image_number('microscope') + 1
        else:
            self.microscope_image_number = 0

        if not first_lensfree_instantiation:
            self.lensfree_image_number = self._get_latest_image_number('lensfree') + 1
        else:
            self.lensfree_image_number = 0
            
        #if not first_reverselens_instantiation:
        #    self.reverselens_image_number = self._get_latest_image_number('reverselens') + 1
        #else:
        #    self.reverselens_image_number = 0

    def save_image(self,image_type,image,metadata,second_image = None):
        image_filename = Path(getattr(self,'sample_savedir_'+image_type),'sample_'+str(self.sample_number)+'_'+image_type+'_'+str(getattr(self,image_type+'_image_number'))+'.tif')
        if second_image is not None:
            second_image_filename = Path(getattr(self,'sample_savedir_'+image_type),'sample_'+str(self.sample_number)+'_'+image_type+'_second_image_'+str(getattr(self,image_type+'_image_number'))+'.tif')
        metadata_filename = Path(getattr(self,'sample_savedir_'+image_type),'sample_'+str(self.sample_number)+'_'+image_type+'_'+str(getattr(self,image_type+'_image_number'))+'.npy')
        if not image_filename.is_file():
            tifffile.imsave(image_filename,image)
            np.save(metadata_filename,metadata)
            setattr(self,image_type+'_image_number',getattr(self,image_type+'_image_number')+1)
        else:
            print(str(getattr(self,image_type+'_image_number')))
            raise ValueError('File already saved?')

        if second_image is not None:
            if not second_image_filename.is_file():
                tifffile.imsave(second_image_filename,second_image)
        
    def get_filename(self,image_type):
        image_filename = Path(getattr(self,'sample_savedir_'+image_type),'sample_'+str(self.sample_number)+'_'+image_type+'_'+str(getattr(self,image_type+'_image_number'))+'.jpeg')
        setattr(self,image_type+'_image_number',getattr(self,image_type+'_image_number')+1)
        return str(image_filename)
        

    def save_schisto_egg_location(self,location,image_size):
        '''
        appends an egg location onto a file saved in the microscope
        '''
        image_type = 'microscope'
        egg_locs_filename = Path(getattr(self,'sample_savedir_'+image_type),'sample_'+str(self.sample_number)+'_microscope_schisto_egg_locs_'+str(getattr(self,image_type+'_image_number'))+'.txt')
        if not egg_locs_filename.is_file():
            with egg_locs_filename.open(mode = 'w+') as egg_file:
                egg_file.write('Full image size: '+str(image_size)+'\n')
                egg_file.write('Egg location recorded: '+str(location)+'\n')
        else:
            with egg_locs_filename.open(mode = 'a') as egg_file:
                egg_file.write('Egg location recorded: '+str(location)+'\n')

    def save_other_egg_location(self,location,image_size):
        '''
        appends an egg location onto a file saved in the microscope
        '''
        image_type = 'microscope'
        egg_locs_filename = Path(getattr(self,'sample_savedir_'+image_type),'sample_'+str(self.sample_number)+'_microscope_other_egg_locs_'+str(getattr(self,image_type+'_image_number'))+'.txt')
        if not egg_locs_filename.is_file():
            with egg_locs_filename.open(mode = 'w+') as egg_file:
                egg_file.write('Full image size: '+str(image_size)+'\n')
                egg_file.write('Egg location recorded: '+str(location)+'\n')
        else:
            with egg_locs_filename.open(mode = 'a') as egg_file:
                egg_file.write('Egg location recorded: '+str(location)+'\n')


    def _generate_save_directory(self,image_type):
        '''
        Makes a savedir and reports if it was already there
        '''

        self.sample_savedir = Path(self.parent_save_directory,str(self.sample_number),image_type)
        if not self.sample_savedir.is_dir():
            self.sample_savedir.mkdir(parents=True)
            return self.sample_savedir,True
        else:
            return self.sample_savedir,False

    def _get_latest_image_number(self,image_type):
        '''
        If images have already been taken, gets the latest suffix.
        '''
        #returns .tif images sorted by the string suffix
        images = sorted([path for path in sorted(getattr(self,'sample_savedir_'+image_type).glob('*.tif'))],key = lambda x: hf.get_int_from_string(x.stem,-1,direction = -1))
        if len(images) == 0:
            return 0
        else:
            return hf.get_int_from_string(images[-1].stem,-1,direction = -1)
