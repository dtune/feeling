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
-- Table structure for table `insurance_master`
--

DROP TABLE IF EXISTS `insurance_master`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `insurance_master` (
  `code` varchar(3) NOT NULL,
  `name` varchar(100) DEFAULT NULL,
  `group_id` varchar(1) DEFAULT NULL,
  `category` varchar(1) NOT NULL DEFAULT '0',
  `dividend_flag` varchar(1) NOT NULL DEFAULT '0',
  `special_flag` varchar(1) NOT NULL DEFAULT '0',
  `bonus_flag` varchar(1) NOT NULL DEFAULT '0',
  `paidup_flag` varchar(1) NOT NULL DEFAULT '0',
  `extend_flag` varchar(1) NOT NULL DEFAULT '0',
  `deathBenefit_flag` varchar(1) NOT NULL DEFAULT '0',
  `unpaidAnnuity_flag` varchar(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `insurance_master`
--

LOCK TABLES `insurance_master` WRITE;
/*!40000 ALTER TABLE `insurance_master` DISABLE KEYS */;
INSERT INTO `insurance_master` VALUES ('001','終身保険','1','0','0','1','1','1','1','0','0'),('002','定期保険','1','0','0','1','0','1','0','0','0'),('003','養老保険','1','0','0','1','0','1','1','0','0'),('004','５年ごと利差配当付個人年金保険（005含む）','1','0','1','0','1','1','0','1','1'),('007','特定疾病保障定期保険',NULL,'0','0','0','0','0','0','0','0'),('008','５年ごと利差配当付特定疾病保障終身保険','1','0','1','0','0','1','1','0','0'),('009','５年ごと利差配当付こども保険','1','0','1','0','0','1','0','1','1'),('011','５年ごと利差配当付終身保険','1','0','1','1','1','1','1','0','0'),('013','５年ごと利差配当付養老保険','1','0','1','1','0','1','1','0','0'),('014','逓増定期保険','1','0','0','1','0','1','0','0','0'),('016','低解約返戻金型終身保険',NULL,'0','0','1','1','1','1','0','0'),('017','５年ごと利差配当付低解約返戻金型終身保険',NULL,'0','1','1','1','1','1','0','0'),('027','低解約返戻金型終身保険',NULL,'0','0','0','0','1','0','1','0'),('028','収入保障保険',NULL,'0','0','1','0','0','0','0','1'),('029','低解約返戻金型長期定期保険',NULL,'0','0','1','0','1','0','0','0'),('030','低解約返戻金収入保障保険',NULL,'0','0','1','0','0','0','0','1'),('031','医療保険（032､033､034含む）',NULL,'0','0','1','0','0','0','0','0'),('035','解約返戻金抑制型医療保険(036含む)',NULL,'0','0','1','0','0','0','0','0'),('042','災害個人年金保険',NULL,'0','1','0','0','1','0','1','1'),('043','積立利率変動型個人年金保険',NULL,'0','0','0','0','0','0','1','1'),('201','傷害特約',NULL,'1','0','0','0','0','0','0','0'),('202','災害入院特約',NULL,'1','0','0','0','0','0','0','0'),('203','疾病入院特約',NULL,'1','0','1','0','0','0','0','0'),('204','災害退院後療養特約',NULL,'1','0','0','0','0','0','0','0'),('205','疾病退院後療養特約',NULL,'1','0','1','0','0','0','0','0'),('206','成人病保障特約',NULL,'1','0','0','0','0','0','0','0'),('208','女性医療特約',NULL,'1','0','0','0','0','0','0','0'),('209','こども医療特約',NULL,'1','0','0','0','0','0','0','0'),('221','七大生活習慣病特約(222含む)',NULL,'1','0','1','0','0','0','0','0'),('223','三大疾病入院一時金特約(224含む)',NULL,'1','0','0','0','0','0','0','0'),('225','先進医療特約',NULL,'1','0','0','0','0','0','0','0'),('226','特定在宅治療支援特約',NULL,'1','0','0','0','0','0','0','0'),('235','退院後療養特約（235,236,237,238）',NULL,'1','0','1','0','0','0','0','0'),('261','がん入院特約(262,263,264含む)',NULL,'1','0','0','0','0','0','0','0'),('265','がん診断給付金特約(266,267,268含む)',NULL,'1','0','0','0','0','0','0','0'),('276','無事故給付金特約(278含む)',NULL,'1','0','0','0','0','0','0','0'),('301','平準定期保険特約',NULL,'1','1','1','0','0','0','0','0'),('302','配偶者定期保険特約',NULL,'1','1','0','0','0','0','0','0'),('303','こども定期保険特約',NULL,'1','1','0','0','0','0','0','0'),('304','逓減定期保険特約',NULL,'1','1','1','0','0','0','0','0'),('306','災害割増特約',NULL,'1','0','0','0','0','0','0','0'),('307','生存給付金付定期保険特約',NULL,'1','1','1','0','0','0','0','0'),('308','収入保障特約',NULL,'1','1','1','0','0','0','0','1'),('309','特定疾病保障定期保険特約',NULL,'1','1','0','0','0','0','0','0'),('310','がん保障定期保険特約',NULL,'1','0','0','0','0','0','0','0'),('311','低解約返戻金型収入保障特約',NULL,'1','0','1','0','0','0','0','1'),('331','終身保険特約',NULL,'1','0','1','0','0','0','0','0'),('332','定期保険特約',NULL,'1','0','1','0','0','0','0','0'),('931','５年ごと利差配当付年金支払移行特約(932,933,934含む)',NULL,'1','0','0','0','0','0','0','1');
/*!40000 ALTER TABLE `insurance_master` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2011-09-09 15:35:27
