# Firebase Face Detection

[Firebase ML Kit](https://firebase.google.com/docs/ml-kit/) was introduced to us at Google I/O '18.
It is a mobile SDK that enables Android and iOS app developers to have advanced machine learning capabilities into their apps with ease.

What is firebase Face detection?
With ML Kit's face detection API, you can detect faces in an image, identify key facial features, and get the contours of detected faces.
With face detection, you can get the information you need to perform tasks like embellishing selfies and portraits or generating avatars from a user's photo. Because ML Kit can perform face detection in real time, you can use it in applications like video chat or games that respond to the player's expressions.
Face detection is the process of automatically locating human faces in visual media (digital images or videos). A face that is detected is reported at a position with an associated size and orientation. Once a face is detected, it can be searched for landmarks such as the eyes and nose.
When talking about face recognition in firebase we must know some key features. Let’s talk about the key factors so you can easily get an idea what the hell is this.
•	Face tracking</br>
•	Landmark </br>
•	Contour </br>
•	Classification 

Face tracking
Face tracking can be done in both images and video. In the videos, it is happening in real time. From the firebase SDK, it can track multiple faces at one. For tracking firebase using angle called Euler. Such as Euler X, Euler Y and Euler Z. ML Kit always reports the Euler Z angle of a detected face. The Euler Y angle is available only when using the "accurate" mode setting of the face detector (as opposed to the "fast" mode setting, which takes some shortcuts to make detection faster). The Euler X angle is not supported.
Landmark
In the ML kit face is categorizes to different landmarks such as mouth, nose, left eye, right eye, etc. This is because it is easy to supply data under the landmark. ML kit first recognizes the whole face. Landmark detection is an optional feature that you need to enable if you wish to use it. The following table will show you all the landmarks which can be identified on your face and their respective Euler angle.

<img src="/images/1.PNG" ></img>


Implementation

For firebase face detection we are going to use FirebaseVisionFaceDetector 
FirebaseVisionImage libraries.

<img src="/images/6.jpg" ></img>
<img src="/images/7.jpg" ></img>
