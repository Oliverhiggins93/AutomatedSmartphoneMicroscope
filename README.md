# AutomatedSmartphoneMicroscopeForDiagnosisOfHelminths

This is the github repo corresponding to the PhD thesis of Oliver Higgins "Deep-learning-assisted portable microscopy for disease diagnosis in low-resource settings". 

The project concerns the creation of a smartphone controlled portable microscope, which was used to detect Helminth eggs in faecal smears. The micrscope app includes an EfficientDet-based object detection algorithm which detects the eggs of the six most common spcies of helminth in images.

# Documentation 

Documentation for building the hardware and using the software is available at: https://oliverhiggins93.github.io/AutomatedSmartphoneMicroscope/

# Dataset 

A dataset of over 12,000 images of Helminth eggs was created as part of this project. The dataset includes images taken with standard brightfield microscopes (10x, 20x and 40 objectives) and the portable automated microscope (10x objective). The annotations were made in PASCAL VOC format and confirmed by staff at the Ministry of Health Uganda, Vector Control Division in Kampala. The dataset is available on Kaggle:

Dataset: https://www.kaggle.com/datasets/oliverjackhiggins/helminth-image-dataset