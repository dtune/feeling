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
-- Temporary table structure for view `v_ins_unit_fund_mapping`
--

DROP TABLE IF EXISTS `v_ins_unit_fund_mapping`;
/*!50001 DROP VIEW IF EXISTS `v_ins_unit_fund_mapping`*/;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
/*!50001 CREATE TABLE `v_ins_unit_fund_mapping` (
  `insurance_code` varchar(3),
  `generation` int(1),
  `payment` int(1),
  `pvh` varchar(1),
  `dividend` int(1),
  `i` double,
  `rate_table_1` varchar(4),
  `rate_table_2` varchar(4),
  `rate_table_3` varchar(4),
  `rate_table_4` varchar(4),
  `limit_age` int(3),
  `limit_sex` varchar(6)
) ENGINE=MyISAM */;
SET character_set_client = @saved_cs_client;

--
-- Final view structure for view `v_ins_unit_fund_mapping`
--

/*!50001 DROP TABLE IF EXISTS `v_ins_unit_fund_mapping`*/;
/*!50001 DROP VIEW IF EXISTS `v_ins_unit_fund_mapping`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8 */;
/*!50001 SET character_set_results     = utf8 */;
/*!50001 SET collation_connection      = utf8_general_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50001 VIEW `v_ins_unit_fund_mapping` AS select `rm`.`insurance_code` AS `insurance_code`,`rm`.`generation` AS `generation`,`rm`.`payment` AS `payment`,`rm`.`pvh` AS `pvh`,`rm`.`dividend` AS `dividend`,`rm`.`i` AS `i`,`mm`.`life_table1_no` AS `rate_table_1`,ifnull(`mm`.`life_table2_no`,'') AS `rate_table_2`,ifnull(`mm`.`life_table3_no`,'') AS `rate_table_3`,ifnull(`mm`.`life_table4_no`,'') AS `rate_table_4`,`mm`.`limit_age` AS `limit_age`,`mm`.`limit_sex` AS `limit_sex` from (`rate_master` `rm` join `mortality_mapping` `mm`) where ((`rm`.`insurance_code` = `mm`.`insurance_code`) and (`mm`.`xyz` = 'x') and ((`rm`.`pvh` = `mm`.`pvh`) or isnull(`mm`.`pvh`)) and ((`rm`.`generation` = `mm`.`generation`) or (`mm`.`generation` = 0))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2011-09-09 15:35:28
