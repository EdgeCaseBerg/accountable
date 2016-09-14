

Accountable [![Build Status](https://travis-ci.org/EdgeCaseBerg/accountable.svg?branch=master)](https://travis-ci.org/EdgeCaseBerg/accountable)
=============================================================

_Turning your weekly expenses into something worthwhile._



### What?

This is a **single user** application meant to help you keep track of
your expenses and then convert that cost into a positive gain for your
body by billing you in exercise. 

### Why?

Because I have over two years worth of habitual spending data from my
[BGI project] and I think it'd be fun to use it to get into shape. Plus
the idea of invoicing myself in exercise amuses me. 



Local Setup
-------------------------------------------------------------

### Requirements:

- Java 8
- MySQL 5.5 or Higher

### Additional:

Install MySQL and then setup the dev database and user:

	CREATE DATABASE accountable DEFAULT CHARACTER SET utf8mb4 DEFAULT COLLATE utf8mb4_unicode_ci;
	CREATE DATABASE accountable_test DEFAULT CHARACTER SET utf8mb4 DEFAULT COLLATE utf8mb4_unicode_ci;
	CREATE USER 'user'@'localhost' IDENTIFIED BY 'password';
	GRANT ALL ON accountable.* TO 'user'@'localhost';
	GRANT ALL ON accountable_test.* TO 'user'@'localhost';
	FLUSH PRIVILEGES

Also, be sure to set your MySQL server to run in UTC time. You can do 
this by updating your my.cnf file and setting:

	[mysqld]
	default-time-zone='+00:00'

From there you can run `sbt` and then `flywayMigrate` to migrate to the
newest database. Note that the application will be pretty useless until
you record some of your expenses into it.



[BGI Project]:https://github.com/EdgeCaseBerg/BGI/
