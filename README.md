[![Build Status](http://img.shields.io/travis/ntoskrnl/AndroidWidgets/master.svg?style=flat)](https://travis-ci.org/ntoskrnl/AndroidWidgets) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.cardiomood.android/android-widgets/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/com.cardiomood.android/android-widgets) AndroidWidgets
==============

Custom Android widgets used in cardiomood.com projects.

## Import to your project

The library is available in maven repository.

You can include your library in your **build.gradle**:

```
compile 'com.cardiomood.android:android-widgets:0.1.1'
```

If you are using Maven, add the following dependency to **pom.xml**:
```xml
<dependency>
  <groupId>com.cardiomood.android</groupId>
  <artifactId>android-widgets</artifactId>
  <version>0.1.1</version>
  <type>aar</type>
</dependency>
```

## Widgets

At the moment, the library includes the following components:
- **SpeedometerGauge** - a simple needle gauge that looks like speedometer
- **BatteryIndicatorGauge** - an iPhone like pseudo-3d battery indicator
- **CircularProgressBar** - a siple circular progress bar with a text inside
- **CircledTextView** - a TextView that has a circle around it


## SpeedometerGauge

This was moved from the previous repository: https://github.com/ntoskrnl/SpeedometerView 

Simple speedometer-like gauge with needle for Android.

![speedometerview-v1](https://f.cloud.github.com/assets/1446492/2292440/175bd3a8-a059-11e3-8f1e-67624fc92349.png)


### Usage

Import the library to your project.

In your layout xml-file add SpeedometerGauge as shown:

```xml
<com.cardiomood.android.controls.gauge.SpeedometerGauge
    android:layout_height="wrap_content"
    android:layout_width="match_parent"
    android:padding="8dp"
    android:id="@+id/speedometer" />
```

Configure SpeedometerGuge:

```java
  private SpeedometerGauge speedometer;

  // Customize SpeedometerGauge
  speedometer = (SpeedometerGauge) v.findViewById(R.id.speedometer);
  
  // Add label converter
  speedometer.setLabelConverter(new SpeedometerView.LabelConverter() {
      @Override
      public String getLabelFor(double progress, double maxProgress) {
          return String.valueOf((int) Math.round(progress));
      }
  });
  
  // configure value range and ticks
  speedometer.setMaxSpeed(300);
  speedometer.setMajorTickStep(30);
  speedometer.setMinorTicks(2);
  
  // Configure value range colors
  speedometer.addColoredRange(30, 140, Color.GREEN);
  speedometer.addColoredRange(140, 180, Color.YELLOW);
  speedometer.addColoredRange(180, 400, Color.RED);

```
