import android.Keys._

android.Plugin.androidBuild

name := "keepshare"

resolvers ++= Resolver.sonatypeRepo("snapshots") ::
  ("circular reveal" at "https://jitpack.io") :: Nil

javacOptions in Global ++= "-target" :: "1.7" :: "-source" :: "1.7" :: Nil

scalacOptions in Global += "-feature"

scalaVersion in Global := "2.11.6"

retrolambdaEnable in Android := false

debugIncludesTests in Android := false

libraryDependencies ++= Seq(
  "ch.acra" % "acra" % "4.6.1",
  "com.rengwuxian.materialedittext" % "library" % "2.0.3" exclude("com.android.support", "appcompat-v7"),
  "com.melnykov" % "floatingactionbutton" % "1.3.0" exclude("com.android.support", "appcompat-v7"),
  "com.github.ozodrukh" % "CircularReveal" % "1.0.6",
  "com.hanhuy" %% "android-common" % "0.4-SNAPSHOT",
  "com.hanhuy.keepassj" % "keepassj" % "2.29.4" exclude("xpp3", "xpp3"),
  "com.google.code.findbugs" % "jsr305" % "2.0.1",
  "com.google.code.gson" % "gson" % "2.2.4",
  "com.android.support" % "support-v4" % "22.1.0",
  "com.android.support" % "appcompat-v7" % "22.1.0",
  "io.reactivex" %% "rxscala" % "0.24.1",
  "io.reactivex" % "rxandroid" % "0.24.0",
  "com.google.android.gms" % "play-services-drive" % "7.0.0"
)

proguardOptions in Android ++=
  "-keepclassmembers class scala.runtime.RichInt { ** until(); }" ::
  "-dontwarn javax.naming.**" ::
  "-dontwarn sun.misc.Unsafe" ::
  Nil

proguardCache in Android += ProguardCache("com.google.common") % "com.google.guava"

ndkBuild in Android := Nil

run <<= run in android.Keys.Android

proguardOptions in Android ++=
  "-keep class * extends junit.framework.TestCase { *; }" ::
  "-keep class scala.runtime.BoxesRunTime { *; }" :: Nil // for debugging only
