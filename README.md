## Meteor

[![Build Status](https://travis-ci.org/xk11961677/framework.svg?branch=master)](https://travis-ci.org/xk11961677/framework)
[![license](https://img.shields.io/badge/license-mit-ff69b4.svg)](https://mit-license.org/license.html)
[![springboot](https://img.shields.io/badge/springboot-2.1.4.RELEASE-orange.svg)](https://spring.io/projects/spring-boot)
![gitter](https://img.shields.io/gitter/room/sky-docs/community)
![version](https://img.shields.io/badge/version-0.1.0-blueviolet.svg)
![helloworld](https://img.shields.io/badge/hello-world-blue)
[![codecov](https://codecov.io/gh/xk11961677/framework/branch/master/graph/badge.svg)](https://codecov.io/gh/xk11961677/framework)

远程调用框架
===


背景
---


项目简介
---


项目架构
---


使用指南
---

开源地址
---
* https://github.com/xk11961677/sky-meteor

版本说明
---
* 暂时0.1.0版本

版权说明
---
* [The MIT License (MIT)](LICENSE)



问题反馈
---
* 在使用中有任何问题，欢迎反馈

开发计划
---

关于作者
---
* name:  sky 
* email: shen11961677@163.com

## 其他说明

### git 分支开发规约
- 使用git flow 流程，分支名称分别以 feature-* 、 release-* 、hotfix-* 开头
- 版本号：<主版本>.<次版本>.<增量版本>-<代号>
   -  方式1: 升级版本号命令: mvn versions:set -DnewVersion=x.x.x
   -  方式2: 升级版本号命令
        -  mvn release:prepare  
            1. 把项目打一个release版本
            2. 在git的tag中打一个tag
            3. 自动升级SNAPSHOT 并提交更新后的pom文件到git
        -  mvn release:rollback
            1. 回滚,但不会删除tag
        -  mvn release:perform  
            1. 去git的tag上拿代码
            2. 用tag上的代码，打一个release版的包 
            3. deploy到的maven私服
   -  方式3: 仅修改 pom.xml --> <revision>1.0.0-SNAPSHOT</revision> 属性
   -  推荐方式3
- 代号版本
    - SNAPSHOT: 用于develop/hotfix
    - RC数字: 用于测试阶段
    - RELEASE: 正式发布版
    - 具体列子:
        1. 开发版本: 1.1.0-SNAPSHOT、1.2.0-SNAPSHOT、2.1.0-SNAPSHOT
        2. 稳定版本:
            1. 候选发布版本: 1.1.0-RC1、1.2.0-RC2
            2. 正式发布版本: 1.1.0-RELEASE、1.1.1-RELEASE

### git message 规约
#### 作用
* 生成规范的 changelog 文件
#### 提交格式
* [请点我](docs/script/changelog/commit.md)
#### 插件
* idea 可使用 git commit template 插件
* npm 可以使用 commitizen

#### 生成changelog方式
* 运行docs/script/changelog/gitlog.sh

