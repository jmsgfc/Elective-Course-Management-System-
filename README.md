# 选课管理系统（Course Selection Management System）

一个基于 Java Swing + MySQL 的桌面端选课管理系统，提供**管理员 / 教师 / 学生**三类角色登录与业务操作，包括课程管理、选课、成绩录入与查询、人员信息维护、统计查询等。

> 适用场景：课程选课与成绩管理的教学演示 / 课程设计 / 小型信息管理系统原型。

---

## 项目做什么

系统围绕“课程—学生—教师—成绩”四类核心数据开展：

- **学生端**：查看课程、选择/退选课程、查询成绩、维护个人信息。
- **教师端**：管理授课课程、录入/修改成绩、查看个人信息。
- **管理员端**：维护学生/教师信息、查看统计信息与成绩总表。

---

## 技术栈

### 后端（业务与数据访问）

本项目是**桌面应用**，没有传统意义的 Web 后端服务。这里的“后端”主要指：业务逻辑 + 数据库访问层。

- **Java**（JDK 8+）
- **JDBC** 访问数据库
- **MySQL Connector/J**（项目内 `libs/mysql-connector-j-9.3.0.jar`）
- 数据库工具类：`src/DButil/DButil.java`（封装获取连接）

### 前端（界面）

- **Java Swing**（桌面 GUI）
- 主要界面入口与面板：
  - `src/gui/adminGUI.java`
  - `src/gui/teaGUI.java`
  - `src/gui/stuGUI.java`
  - 以及 `src/admin/*`、`src/teacher/*`、`src/student/*` 下的各类功能面板

---

## 目录结构（简要）

- `src/Login/`：登录相关
- `src/gui/`：三端主界面（管理员/教师/学生）
- `src/admin/`：管理员端功能面板
- `src/teacher/`：教师端功能面板
- `src/student/`：学生端功能面板
- `src/DButil/`：数据库连接工具
- `src/info/`：实体/信息类（课程、学生、教师、成绩等）
- `libs/`：第三方依赖（MySQL JDBC 驱动）
- `studentoperation.sql`：数据库初始化脚本

---

## 快速上手（在别的电脑上快速部署）

下面以 **Windows + IntelliJ IDEA** 为例。

### 1) 运行环境与所需工具

- **JDK**：建议 JDK 8+（更高版本也可，一般兼容）
- **IDE**：IntelliJ IDEA
- **数据库**：MySQL 8.0+（你也可以用更高版本）
- **数据库驱动**：项目已自带 `libs/mysql-connector-j-9.3.0.jar`

> 如果你使用的是 IntelliJ IDEA，打开项目后一般会自动识别 `libs/` 里的 jar 依赖；如未识别，可在 Project Structure → Modules → Dependencies 手动将该 jar 添加为依赖。

### 2) 初始化数据库

1. 启动 MySQL，创建数据库：

```sql
CREATE DATABASE IF NOT EXISTS studentoperation DEFAULT CHARACTER SET utf8mb4;
```

2. 导入初始化脚本 `studentoperation.sql`（任选一种方式）：

- **方式 A：使用图形化工具**（如 MySQL Workbench / Navicat）
  - 选择刚创建的 `studentoperation` 数据库
  - 运行项目根目录下的 `studentoperation.sql`

- **方式 B：使用命令行**（示例）

```bash
mysql -u root -p studentoperation < studentoperation.sql
```

### 3) 配置数据库账号密码

项目已将数据库连接配置改为读取根目录下的 `config.properties`，修改该配置文件即可：

- `jdbc.url`
- `jdbc.username`
- `jdbc.password`

> 说明：程序启动时会优先从 *classpath* 读取 `config.properties`；若未找到，会尝试从**项目运行目录（通常是项目根目录）**读取同名文件。

### 4) 运行项目

- 在 IDEA 中找到并运行登录入口：`src/Login/Login.java`（或 `src/Login/userLogin.java`，以你项目内实际入口为准）
- 运行后根据角色账号登录

#### 测试账号（来自 `使用手册.txt`）

- 学生：账号 `101` / 密码 `111222`
- 教师：账号 `0001` / 密码 `123456`
- 管理员：账号 `admin` / 密码 `admin`

---

## 功能模块说明

> 下面按角色概览功能模块（以 `src/admin`、`src/teacher`、`src/student` 目录内面板类为参考）。

### 管理员端（Admin）

- **学生管理**：维护学生信息（增删改查等）
  - 相关面板：`src/admin/StudentManagementPanel.java`
- **教师管理**：维护教师信息（增删改查等）
  - 相关面板：`src/admin/TeacherManagementPanel.java`
- **统计/成绩总表**：成绩总表查询、统计相关功能
  - 相关面板：`src/admin/StatisticsPanel.java`

### 教师端（Teacher）

- **课程管理/授课信息**：查看与管理教师的课程信息
  - 相关面板：`src/teacher/CoursePanel.java`
- **成绩管理**：成绩录入、修改、查询
  - 相关面板：`src/teacher/ScorePanel.java`
- **个人信息**：查看个人资料
  - 相关面板：`src/teacher/ProfilePanel.java`

### 学生端（Student）

- **课程浏览**：查看课程信息
  - 相关面板：`src/student/CourseViewPanel.java`
- **选课/退选**：选择课程、管理已选课程
  - 相关面板：`src/student/CourseSelectionPanel.java`
- **成绩查询**：查询个人成绩
  - 相关面板：`src/student/ScoreQueryPanel.java`
- **个人信息**：查看/维护个人信息
  - 相关面板：`src/student/PersonalInfoPanel.java`

---

## 常见问题（FAQ）

### Q1：换电脑后提示找不到 MySQL 驱动 / 无法连接数据库？

- 确认项目目录下存在 `libs/mysql-connector-j-9.3.0.jar`
- 在 IDEA 中确认该 jar 已被添加到 Module Dependencies
- 确认 MySQL 已启动，且 `DButil.java` 里的 `url/name/pwd` 配置正确

### Q2：数据库导入后仍报表不存在？

- 确认导入的库名是 `studentoperation`
- 确认 `DButil.java` 的连接 URL 指向相同库名

### Q3：提示“读取 config.properties 失败”或“配置文件缺少键”？

- 确认项目根目录存在 `config.properties`
- 确认包含以下 3 个键：`jdbc.url`、`jdbc.username`、`jdbc.password`
- 确认运行时工作目录为项目根目录（IDEA: Run/Debug Configurations → Working directory）

---

## 许可与声明

本项目用于学习与课程设计用途。若用于生产环境，请完善权限控制、异常处理、配置管理（建议把数据库账号密码改为可配置文件形式）。
