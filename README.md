<div align="center">

# Note Community Backend

一个面向学习场景的笔记社区后端服务，提供用户、笔记、题目、评论、消息、搜索与统计等核心能力。

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![MyBatis](https://img.shields.io/badge/MyBatis-2.2.0-DC382D?style=flat-square)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat-square&logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-Cache-DC382D?style=flat-square&logo=redis&logoColor=white)
![Elasticsearch](https://img.shields.io/badge/Elasticsearch-Search-005571?style=flat-square&logo=elasticsearch&logoColor=white)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-Message-FF6600?style=flat-square&logo=rabbitmq&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-Auth-000000?style=flat-square&logo=jsonwebtokens&logoColor=white)
![Knife4j](https://img.shields.io/badge/Knife4j-API%20Docs-409EFF?style=flat-square)
![Log4j2](https://img.shields.io/badge/Log4j2-Logging-FF6600?style=flat-square&logo=apache&logoColor=white)

</div>

---

## 项目介绍

Note Community Backend 是一个基于 Spring Boot 的 RESTful API 服务，面向“题目练习 + 笔记沉淀 + 社区互动”场景设计。
系统支持账号/邮箱登录、笔记发布与互动、题库与题单管理、消息通知、全文搜索与运营统计，适用于学习社区或知识平台后端建设。

---

## 技术栈

| 类别 | 技术 | 说明 |
|---|---|---|
| 语言与框架 | ![Java](https://img.shields.io/badge/Java-17-ED8B00?style=flat-square&logo=openjdk&logoColor=white) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-6DB33F?style=flat-square&logo=springboot&logoColor=white) | 应用基础框架 |
| 安全认证 | ![Spring Security](https://img.shields.io/badge/Spring%20Security-Auth-6DB33F?style=flat-square&logo=springsecurity&logoColor=white) ![JWT](https://img.shields.io/badge/JWT-Token-000000?style=flat-square&logo=jsonwebtokens&logoColor=white) | 认证与鉴权 |
| 数据访问 | ![MyBatis](https://img.shields.io/badge/MyBatis-2.2.0-DC382D?style=flat-square) | ORM / SQL 映射 |
| 数据存储 | ![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat-square&logo=mysql&logoColor=white) ![Redis](https://img.shields.io/badge/Redis-Cache-DC382D?style=flat-square&logo=redis&logoColor=white) | 持久化与缓存 |
| 搜索引擎 | ![Elasticsearch](https://img.shields.io/badge/Elasticsearch-7.x-005571?style=flat-square&logo=elasticsearch&logoColor=white) | 笔记/用户搜索 |
| 消息队列 | ![RabbitMQ](https://img.shields.io/badge/RabbitMQ-Async-FF6600?style=flat-square&logo=rabbitmq&logoColor=white) | 异步邮件任务 |
| 文档与日志 | ![Knife4j](https://img.shields.io/badge/Knife4j-API%20Docs-409EFF?style=flat-square) ![Log4j2](https://img.shields.io/badge/Log4j2-Logging-FF6600?style=flat-square&logo=apache&logoColor=white) | 接口文档与日志治理 |
| 其他组件 | ![Thymeleaf](https://img.shields.io/badge/Thymeleaf-Template-005F0F?style=flat-square&logo=thymeleaf&logoColor=white) | 邮件模板渲染 |

---

## 系统架构图

```mermaid
graph TB
    subgraph Client["客户端"]
        WEB["Web 前端"]
        ADMIN["管理后台"]
    end

    subgraph Access["接入层"]
        API["REST API (Spring MVC)"]
        AUTH["JWT + Security + Interceptor/AOP"]
        DOC["Knife4j 文档"]
    end

    subgraph Biz["业务层"]
        USER["用户模块"]
        NOTE["笔记模块"]
        QUESTION["题目/题单模块"]
        COMMENT["评论模块"]
        MSG["消息模块"]
        SEARCH["搜索模块"]
        STAT["统计模块"]
        FILE["上传模块"]
    end

    subgraph Infra["基础设施层"]
        MYBATIS["MyBatis Mapper/XML"]
        REDIS["Redis 缓存"]
        MQ["RabbitMQ 异步任务"]
        ES["Elasticsearch 检索"]
        MAIL["Mail + Thymeleaf"]
        SCHED["Scheduled 定时任务"]
    end

    subgraph Data["数据层"]
        MYSQL[("MySQL")]
    end

    WEB --> API
    ADMIN --> API
    API --> AUTH
    API --> Biz
    Biz --> MYBATIS
    Biz --> REDIS
    Biz --> MQ
    Biz --> ES
    MQ --> MAIL
    SCHED --> MYSQL
    MYBATIS --> MYSQL
```

---

## 核心功能

- 用户体系：注册、登录、自动登录、个人资料维护、头像上传。
- 笔记体系：笔记发布/编辑/删除、点赞、收藏、排行榜与热力图。
- 题库体系：题目查询、分类管理、题单管理、完成状态追踪。
- 社区互动：评论、回复、消息通知、未读管理。
- 搜索能力：基于 Elasticsearch 的笔记与用户检索（含高亮）。
- 运营统计：定时汇总登录、注册、笔记提交等核心指标。
- 邮件服务：验证码发送、频率限制、异步投递（RabbitMQ）。

---

## 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+
- RabbitMQ 3.8+
- Elasticsearch 7.x

---

## 快速开始

### 1) 克隆项目

```bash
git clone https://github.com/roocl/note-backend.git
cd note-backend
```

### 2) 配置数据库与环境

- 在 MySQL 中创建数据库（例如 `kamanote_tech`）。
- 修改 `src/main/resources/application-dev.yaml` 中的数据源配置。
- 根据本地环境调整 `src/main/resources/application.yaml` 中 Redis、RabbitMQ、Elasticsearch、邮件配置。

### 3) 启动依赖服务

- 启动 MySQL
- 启动 Redis
- 启动 RabbitMQ
- 启动 Elasticsearch

### 4) 构建与运行

```bash
mvn clean compile
mvn spring-boot:run
```

### 5) 访问地址

- 服务地址：`http://localhost:8080`
- API 文档：`http://localhost:8080/doc.html`
