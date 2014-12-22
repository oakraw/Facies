Facies
======

Facies is an Android application, a project of Mahidol U's EGCO486 Image Processing, for swaping faces.

Download APK
------
<a href="https://github.com/oakraw/Facies/blob/master/app/app-release.apk?raw=true">apk file</a>


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

Screenshots
-------
<div style="float:left;">
<img hspace="10" width="25%" src="https://fbcdn-sphotos-h-a.akamaihd.net/hphotos-ak-xpf1/v/t34.0-12/10859307_886229878096563_773386051_n.jpg?oh=dcd597a1fa4e2138c574236597f38787&oe=54997EFF&__gda__=1419418530_63cefa7312ffd5a98d36f8d35f55690b">
<img hspace="10" width="25%" src="https://fbcdn-sphotos-h-a.akamaihd.net/hphotos-ak-xpa1/v/t34.0-12/10863636_886231331429751_1701678231_n.jpg?oh=39589ba5be2c29271f50c2675277ace1&oe=54997263&__gda__=1419337731_cec4631922a9fb6e725737ada85b9e1d">
<img hspace="10" width="25%" src="https://fbcdn-sphotos-h-a.akamaihd.net/hphotos-ak-xpf1/v/t34.0-12/10884650_886231404763077_1393563239_n.jpg?oh=3832e6ef6904d0f38cef35c91888fdd6&oe=54997DB7&__gda__=1419419047_70c33c2b27e6bec0c9800da8e042b39f">
</div>
<br>

<img width="90%" src="https://fbcdn-sphotos-h-a.akamaihd.net/hphotos-ak-xpf1/v/t34.0-12/10872525_886232364762981_1662197789_n.jpg?oh=1c180db14f5ed84cec93e8a48ea44bd2&oe=5499A0E6&__gda__=1419359798_44572697908f42a3389146ccd9c3cefe">
<img width="90%" src="https://fbcdn-sphotos-h-a.akamaihd.net/hphotos-ak-xpa1/v/t34.0-12/10850670_886232314762986_1252779116_n.jpg?oh=1d5aa00262f88ed95540e14665a1cf0f&oe=54997A0F&__gda__=1419336111_fdc8d1ee12ec09451f781d25eae716b6">



*This project was the work of fews people and was done in four days. It is not perfect as claimed :P*
