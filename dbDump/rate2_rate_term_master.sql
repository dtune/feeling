CREATE DATABASE  IF NOT EXISTS `rate2` /*!40100 DEFAULT CHARACTER SET utf8 */;
USE `rate2`;
-- MySQL dump 10.13  Distrib 5.1.40, for Win32 (ia32)
--
-- Host: 192.168.0.190    Database: rate2
-- ------------------------------------------------------
-- Server version	5.1.48-community

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `rate_term_master`
--

DROP TABLE IF EXISTS `rate_term_master`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `rate_term_master` (
  `idrate_term_master` int(11) NOT NULL,
  `insurance_code` varchar(3) NOT NULL,
  `condition` varchar(255) DEFAULT NULL,
  `term_code` int(1) NOT NULL,
  PRIMARY KEY (`idrate_term_master`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rate_term_master`
--

LOCK TABLES `rate_term_master` WRITE;
/*!40000 ALTER TABLE `rate_term_master` DISABLE KEYS */;
INSERT INTO `rate_term_master` VALUES (1,'004','(n+g/2)<=12.5',1),(2,'004','12.5<(n+g/2)<=17.5',2),(3,'004','(n+g/2)>17.5',3),(4,'005','(n+g/2)<=12.5',1),(5,'005','12.5<(n+g/2)<=17.5',2),(6,'005','(n+g/2)>17.5',3),(7,'008',NULL,3),(8,'009',NULL,1),(9,'011',NULL,3),(10,'013',NULL,1),(11,'017',NULL,3),(12,'042','(n+g/2)<=12.5',1),(13,'042','12.5<(n+g/2)<=17.5',2),(14,'042','(n+g/2)>17.5',3),(15,'301',NULL,1),(16,'302',NULL,1),(17,'303',NULL,1),(18,'304',NULL,1),(19,'307',NULL,1),(20,'308',NULL,1),(21,'309',NULL,1);
/*!40000 ALTER TABLE `rate_term_master` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2011-09-09 15:35:28
