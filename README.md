Facies
======

Facies is an Android application, a project of Mahidol U's EGCO486 Image Processing, for swaping faces.


Main Feature
------
- Swap faces in photos using face detection feature of Android API and owned algorithm to select only face elements (eyes, nose, mouth)
- Users are able to select faces to swap
- Users can select new undetected faces in photos


Owned Algorithm for Face Selection
------
- Detect glasses in the photo before thresholding
- Adaptive thresholding -> changing threshold of RGB diffirences to get expected area of faces
- Filling -> filling pixels between enable pixels after thresholding if the size is matched
- Fading quadrants -> using functions to fade out the the outer area of face by divide to four quadrants

How to Use
-------
1. Add photo by using the leftest button on the bottom
2. The app will process faces inside the photo. This will take fews seconds to a couple of minutes
3. Photo is showed with green box(es) around the detected face(s)
4. Using the second button to select the face you want to apply to others. There is also the button to create new face frame inside
5. The remove button is the third one from the left. Use it to remove applied faces
6. When finish all funs, use the forth button to save image to your phone storage. Share button will appear when finish saving


*This project was the work of fews people and was done in four days. It is not perfect as claimed :P*
