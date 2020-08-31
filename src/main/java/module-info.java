module lessonplan.fx {

    requires javafx.fxml;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.media;
    requires jfxtras.common;
    requires jfxtras.controls;
    requires jfxtras.fxml;
    requires com.github.albfernandez.juniversalchardet;

    requires java.desktop;

    requires spring.core;
    requires spring.context;
    requires spring.aop;
    requires spring.beans;
    requires spring.boot;
    requires spring.boot.autoconfigure;


    requires static lombok;
    requires com.google.gson;

    requires org.bytedeco.ffmpeg;
    requires org.bytedeco.javacv;
    requires org.bytedeco.javacpp;


    requires org.bytedeco.ffmpeg.windows.x86;
    requires org.bytedeco.javacpp.windows.x86;

    requires org.bytedeco.ffmpeg.windows.x86_64;
    requires org.bytedeco.javacpp.windows.x86_64;

    requires org.bytedeco.ffmpeg.linux.x86;
    requires org.bytedeco.javacpp.linux.x86_64;

    requires org.bytedeco.javacpp.macosx.x86_64;

    requires org.slf4j;

    requires com.google.zxing;
    requires com.google.zxing.javase;

    opens fxml;


    opens ru.otus.lessonplan;
    opens ru.otus.lessonplan.fx;
    opens ru.otus.lessonplan.controllers;
    opens ru.otus.lessonplan.model;
    opens ru.otus.lessonplan.services;
    opens ru.otus.lessonplan.services.lessonplan;
    opens ru.otus.lessonplan.services.lessonplan.strategies;
    opens ru.otus.lessonplan.services.screen;
    opens ru.otus.lessonplan.services.video;

    opens ru.otus.lessonplan.utils;
}