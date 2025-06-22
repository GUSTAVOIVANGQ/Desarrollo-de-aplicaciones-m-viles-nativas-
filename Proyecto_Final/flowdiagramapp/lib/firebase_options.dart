import 'package:firebase_core/firebase_core.dart' show FirebaseOptions;
import 'package:flutter/foundation.dart'
    show defaultTargetPlatform, kIsWeb, TargetPlatform;

/// Default [FirebaseOptions] for use with your Firebase apps.
///
/// Example:
/// ```dart
/// import 'firebase_options.dart';
/// // ...
/// await Firebase.initializeApp(
///   options: DefaultFirebaseOptions.currentPlatform,
/// );
/// ```
class DefaultFirebaseOptions {
  static FirebaseOptions get currentPlatform {
    if (kIsWeb) {
      return web;
    }
    switch (defaultTargetPlatform) {
      case TargetPlatform.android:
        return android;
      case TargetPlatform.iOS:
        return ios;
      case TargetPlatform.macOS:
        return macos;
      case TargetPlatform.windows:
        throw UnsupportedError(
          'DefaultFirebaseOptions have not been configured for windows - '
          'you can reconfigure this by running the FlutterFire CLI again.',
        );
      case TargetPlatform.linux:
        throw UnsupportedError(
          'DefaultFirebaseOptions have not been configured for linux - '
          'you can reconfigure this by running the FlutterFire CLI again.',
        );
      default:
        throw UnsupportedError(
          'DefaultFirebaseOptions are not supported for this platform.',
        );
    }
  }

  static const FirebaseOptions web = FirebaseOptions(
    apiKey: 'AIzaSyDXn57bKp1ayq1QWtstRq_XBI7vY3-C1A8',
    appId: '1:345911525401:web:web_app_id_here',
    messagingSenderId: '345911525401',
    projectId: 'flowdiagram-app',
    authDomain: 'flowdiagram-app.firebaseapp.com',
    storageBucket: 'flowdiagram-app.firebasestorage.app',
  );

  static const FirebaseOptions android = FirebaseOptions(
    apiKey: 'AIzaSyDXn57bKp1ayq1QWtstRq_XBI7vY3-C1A8',
    appId: '1:345911525401:android:25d0c44346688f1964aa88',
    messagingSenderId: '345911525401',
    projectId: 'flowdiagram-app',
    storageBucket: 'flowdiagram-app.firebasestorage.app',
  );

  static const FirebaseOptions ios = FirebaseOptions(
    apiKey: 'AIzaSyA3TPXcCpsQIFcerlusg3StU_foZWph_s0',
    appId: '1:345911525401:ios:c72ae8a4e23c6c9464aa88',
    messagingSenderId: '345911525401',
    projectId: 'flowdiagram-app',
    storageBucket: 'flowdiagram-app.firebasestorage.app',
    iosBundleId: 'com.example.flowdiagramapp',
  );

  static const FirebaseOptions macos = FirebaseOptions(
    apiKey: 'AIzaSyA3TPXcCpsQIFcerlusg3StU_foZWph_s0',
    appId: '1:345911525401:ios:c72ae8a4e23c6c9464aa88',
    messagingSenderId: '345911525401',
    projectId: 'flowdiagram-app',
    storageBucket: 'flowdiagram-app.firebasestorage.app',
    iosBundleId: 'com.example.flowdiagramapp',
  );
}
