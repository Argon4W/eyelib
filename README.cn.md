# Eyelib

基于《Minecraft》的渲染 mod。

[English](README.md) | [中文](README.cn.md)

## 设置

如果你需要在开发环境测试该 mod，则需要将 `build.gradle` 文件中的 `sourceSets.main.resources { exclude 'assets' }` 注释掉。

## 功能

该 mod 的结构基于 `Capability`。

该 mod 的 `Capability` 被附加到大多数游戏对象上，所以你可以通过修改 `Capability` 的内容来修改游戏对象的渲染。

你可以在 `io.github.tt432.eyelib.capability` 包下找到该 mod 的 `Capability`。