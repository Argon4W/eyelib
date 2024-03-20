# Eyelib

the renderer lib for _Minecraft_.

[English](README.md) | [中文](README.cn.md)

## Features

The mod uses a `Capability`-based structure.

`Capability` is attached to a most game elements in the mod, allowing you to easily modify the Capability content in your mod to alter the rendering.

You can find the `Capability` in the `io.github.tt432.eyelib.capability` package.

## Dependencies

- [Antlr-Molang](https://github.com/TT432/antlr-molang) for parse Molang.
- [Manifold: String Templates](https://github.com/manifold-systems/manifold/blob/master/manifold-deps-parent/manifold-strings/README.md) for String Template.
- [javassist](http://www.javassist.org/) for generate bytecode for Molang.
- [lombok](https://projectlombok.org/) for generate getter/setter and more.