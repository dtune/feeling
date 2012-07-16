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
-- Table structure for table `qw_master`
--

DROP TABLE IF EXISTS `qw_master`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `qw_master` (
  `idqw_master` int(11) NOT NULL AUTO_INCREMENT,
  `insurance_code` varchar(3) NOT NULL,
  `generation` int(1) NOT NULL DEFAULT '0' COMMENT '0であれば、世代に関係ない',
  `condition` varchar(255) DEFAULT NULL COMMENT 'nullであれば、pvhに関係ない',
  `pvh` varchar(1) DEFAULT NULL COMMENT 'nullであれば、pvhに関係ない',
  `qw` double DEFAULT '0',
  PRIMARY KEY (`idqw_master`)
) ENGINE=InnoDB AUTO_INCREMENT=159 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qw_master`
--

LOCK TABLES `qw_master` WRITE;
/*!40000 ALTER TABLE `qw_master` DISABLE KEYS */;
INSERT INTO `qw_master` VALUES (1,'030',5,'t<=m&&kaisu>1','p',0.03),(2,'030',5,'t>m',NULL,0),(3,'030',5,'t<=m','v',0.015),(5,'030',5,'t<=m','h',0.015),(8,'031',0,'insuranceCode==31',NULL,0),(9,'031',0,'insuranceCode==33&&(m==99||t<=m)&&kaisu>1','p',0.03),(10,'031',0,'insuranceCode==33&&(m==99||t<=m)','v',0.015),(11,'031',0,'insuranceCode==33&&(m==99||t<=m)','h',0.015),(12,'031',0,'insuranceCode==33&&(m!=99&&t>m)',NULL,0),(13,'029',0,'fundCode==29','p',0.03),(14,'029',0,'fundCode==29','v',0.015),(15,'029',0,'fundCode==29','h',0.015),(16,'029',0,'fundCode==2',NULL,0),(21,'027',3,'1','p',0.03),(22,'027',3,'1','v',0.015),(23,'027',3,'1','h',0.015),(24,'261',0,'kaiyakuUmu==0&&(m==99||t<=m)&&kaisu>1','p',0.03),(25,'261',0,'kaiyakuUmu==0&&(m==99||t<=m)','v',0.015),(26,'261',0,'kaiyakuUmu==0&&(m==99||t<=m)','h',0.015),(27,'261',0,'kaiyakuUmu==0&&(m!=99&&t>m)',NULL,0),(28,'261',0,'kaiyakuUmu==1',NULL,0),(29,'016',0,'fundCode==001',NULL,0),(38,'016',0,'fundCode==016',NULL,0.063),(47,'265',0,'kaiyakuUmu==1',NULL,0),(48,'265',0,'kaiyakuUmu==0&&(t<=m&&kaisu>1)','p',0.03),(49,'265',0,'kaiyakuUmu==0&&t<=m','v',0.015),(50,'265',0,'kaiyakuUmu==0&&t<=m','h',0.015),(51,'265',0,'kaiyakuUmu==0&&t>m',NULL,0),(52,'017',0,'fundCode==011',NULL,0),(61,'017',0,'fundCode==017',NULL,0.063),(89,'276',0,'kaiyakuUmu==1',NULL,0),(90,'276',0,'kaiyakuUmu==0','p',0.03),(91,'276',0,'kaiyakuUmu==0','v',0.015),(92,'276',0,'kaiyakuUmu==0','h',0.015),(93,'311',5,'t<=m&&kaisu>1','p',0.03),(94,'311',5,'t<=m','v',0.015),(95,'311',5,'t<=m','h',0.015),(96,'311',5,'t>m',NULL,0),(97,'235',0,'kaiyakuUmu==1',NULL,0),(106,'235',0,'kaiyakuUmu==0&&(t<=m&&kaisu>1)','p',0.03),(107,'235',0,'kaiyakuUmu==0&&t<=m','v',0.015),(108,'235',0,'kaiyakuUmu==0&&t<=m','h',0.015),(115,'235',0,'kaiyakuUmu==0&&t>m',NULL,0),(124,'031',0,'insuranceCode==32',NULL,0),(125,'031',0,'insuranceCode==34&&(t<=m&&kaisu>1)','p',0.03),(126,'031',0,'insuranceCode==34&&t<=m','v',0.015),(127,'031',0,'insuranceCode==34&&t<=m','h',0.015),(128,'031',0,'insuranceCode==34&&t>m',NULL,0),(129,'035',0,'1','p',0.03),(130,'035',0,'1','p',0.001),(131,'035',0,'1','v',0.015),(132,'035',0,'1','v',0),(133,'035',0,'1','h',0.015),(134,'035',0,'1','h',0),(135,'221',0,'1','p',0.03),(136,'221',0,'1','p',0.001),(137,'221',0,'1','v',0.015),(138,'221',0,'1','v',0),(139,'221',0,'1','h',0.015),(140,'221',0,'1','h',0),(141,'223',0,'1','p',0.03),(142,'223',0,'1','p',0.001),(143,'223',0,'1','v',0.015),(144,'223',0,'1','v',0),(145,'223',0,'1','h',0.015),(146,'223',0,'1','h',0),(147,'226',0,'1','p',0.03),(149,'226',0,'state==1','v',0.015),(150,'226',0,'state==6','v',0),(151,'226',0,'state==1','h',0.015),(152,'226',0,'state==6','h',0),(153,'225',0,'1','p',0.03),(154,'225',0,'state==1','v',0.015),(155,'225',0,'state==1','h',0.015),(157,'225',0,'state==6','v',0),(158,'225',0,'state==6','h',0);
/*!40000 ALTER TABLE `qw_master` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2011-09-09 15:35:26
