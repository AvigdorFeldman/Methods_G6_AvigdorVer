USE bpark;

-- -----------------------
-- Table structure for table `subscribers`
-- -----------------------
DROP TABLE IF EXISTS `subscribers`;
CREATE TABLE IF NOT EXISTS `subscribers` (
  `subscriber_id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `phone` VARCHAR(20) DEFAULT NULL,
  `email` VARCHAR(100) DEFAULT NULL,
  `role` VARCHAR(50) DEFAULT 'USER',
  `tag` VARCHAR(50) DEFAULT NULL,
  `code` INT DEFAULT NULL,
  `logged_in` BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY (`subscriber_id`),
  UNIQUE (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

LOCK TABLES `subscribers` WRITE;
INSERT INTO `subscribers` 
(`subscriber_id`,`name`,`phone`,`email`,`role`,`tag`,`code`,`logged_in`) VALUES
(2001,'Alice','050-123-4567','Avigdor.Feldman@e.braude.ac.il','SUBSCRIBER','EAF2D7E142C05C2A1B29F7A4C0F4D3B2',111111,FALSE),
(2002,'Bob','050-234-5678','bob@example.com','SUBSCRIBER','E593D02B97A67D2BFC229B4A2A6A4B6A',222222,FALSE),
(2003,'Carol','050-345-6789','carol@example.com','SUBSCRIBER','E35A4B571F1A81F5DC129F004A1F620F',333333,FALSE),
(2004,'Dave','050-456-7890','dave@example.com','ATTENDANT','E89772D5A74B905BD1E4936DE07B2F68',444444,FALSE),
(2005,'Eve','050-567-8901','eve@example.com','MANAGER','E453FCE581D2A132FB5C712BE54A5E82',555555,FALSE),
(2006,'Guy','054-246-9952','guyziv313@gmail.com','SUBSCRIBER','821E4FF208EA3AFFD4B9503A',252306,FALSE),
(2007,'Hila','050-678-1234','hila@example.com','SUBSCRIBER','A1B2C3D4E5F6071829304A5B6C7D8E9F',777777,FALSE),
(2008,'Tom','050-987-6543','tom@example.com','SUBSCRIBER','0F1E2D3C4B5A69788796A5B4C3D2E1F0',888888,FALSE);
UNLOCK TABLES;

-- -----------------------
-- Table structure for table `parking_spots`
-- -----------------------
DROP TABLE IF EXISTS `parking_spots`;
CREATE TABLE IF NOT EXISTS `parking_spots` (
  `spot_id` INT NOT NULL AUTO_INCREMENT,
  `status` VARCHAR(20) NOT NULL DEFAULT 'FREE',
  PRIMARY KEY (`spot_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

LOCK TABLES `parking_spots` WRITE;
INSERT INTO `parking_spots` (`spot_id`,`status`) VALUES
(101,'FREE'),(102,'FREE'),(103,'OCCUPIED'),(104,'FREE'),(105,'OCCUPIED'),
(106,'FREE'),(107,'FREE'),(108,'OCCUPIED'),(109,'FREE'),(110,'FREE'),
(111,'FREE'),(112,'FREE'),(113,'FREE'),(114,'OCCUPIED'),(115,'FREE');
UNLOCK TABLES;

-- -----------------------
-- Table structure for table `parking_sessions`
-- -----------------------
DROP TABLE IF EXISTS `parking_sessions`;
CREATE TABLE IF NOT EXISTS `parking_sessions` (
  `session_id` INT NOT NULL AUTO_INCREMENT,
  `subscriber_id` INT NOT NULL,
  `spot_id` INT NOT NULL,
  `parking_code` INT NOT NULL,
  `in_time` DATETIME NOT NULL,
  `out_time` DATETIME NOT NULL,
  `extended` BOOLEAN NOT NULL DEFAULT FALSE,
  `late` BOOLEAN NOT NULL DEFAULT FALSE,
  `active` BOOLEAN NOT NULL DEFAULT TRUE,
  PRIMARY KEY (`session_id`),
  FOREIGN KEY (`subscriber_id`) REFERENCES `subscribers`(`subscriber_id`)
    ON UPDATE CASCADE ON DELETE CASCADE,
  FOREIGN KEY (`spot_id`) REFERENCES `parking_spots`(`spot_id`)
    ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

LOCK TABLES `parking_sessions` WRITE;
INSERT INTO `parking_sessions`
(`subscriber_id`,`spot_id`,`parking_code`,`in_time`,`out_time`,`extended`,`late`,`active`)
VALUES
(2001,103,123001,'2025-05-01 08:00:00','2025-05-01 12:00:00',FALSE,FALSE,TRUE),          
(2002,105,123002,'2025-05-02 09:30:00','2025-05-02 12:30:00',FALSE,FALSE,TRUE),         
(2003,108,123003,'2025-05-03 10:15:00','2025-05-03 13:15:00',TRUE,FALSE,FALSE),          
(2006,106,123006,'2025-07-01 08:00:00','2025-07-01 11:00:00',FALSE,FALSE,FALSE),         
(2007,107,123007,'2025-07-02 09:30:00','2025-07-02 13:30:00',FALSE,FALSE,FALSE),        
(2008,109,123008,'2025-07-03 14:00:00','2025-07-03 17:00:00',TRUE,FALSE,TRUE),          
(2006,112,123009,'2025-07-04 07:45:00','2025-07-04 09:45:00',FALSE,FALSE,FALSE),        
(2007,113,123010,'2025-07-05 16:00:00','2025-07-05 19:00:00',FALSE,FALSE,TRUE);           
UNLOCK TABLES;

-- -----------------------
-- Table structure for table `reservations`
-- -----------------------
DROP TABLE IF EXISTS `reservations`;
CREATE TABLE IF NOT EXISTS `reservations` (
  `reservation_id` INT NOT NULL AUTO_INCREMENT,
  `subscriber_id` INT NOT NULL,
  `spot_id` INT NOT NULL,
  `date` DATE NOT NULL,
  `start_time` TIME DEFAULT NULL,
  `end_time` TIME DEFAULT NULL,
  `reservation_code` INT NOT NULL,
  PRIMARY KEY (`reservation_id`),
  FOREIGN KEY (`subscriber_id`) REFERENCES `subscribers`(`subscriber_id`)
    ON UPDATE CASCADE ON DELETE CASCADE,
  FOREIGN KEY (`spot_id`) REFERENCES `parking_spots`(`spot_id`)
    ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

LOCK TABLES `reservations` WRITE;
INSERT INTO `reservations` 
(`reservation_id`,`subscriber_id`,`spot_id`,`date`,`start_time`,`end_time`,`reservation_code`)
VALUES
(1,  2002, 101, '2025-05-10', '09:00:00', '11:00:00', '843201'),
(2,  2003, 102, '2025-05-11', '14:30:00', '16:30:00', '539812'),
(3,  2004, 104, '2025-05-12', '08:15:00', '10:15:00', '692340'),
(4,  2005, 110, '2025-05-13', '12:00:00', '14:00:00', '417689'),
(5,  2001, 109, '2025-05-14', '10:00:00', '12:00:00', '705436'),
(6,  2006, 106, '2025-07-10', '10:00:00', '14:00:00', '134728'),    
(7,  2007, 107, '2025-07-10', '10:00:00', '14:00:00', '986523'),   
(8,  2008, 109, '2025-07-11', '15:00:00', '17:00:00', '248915'),    
(9,  2006, 112, '2025-07-12', '09:00:00', '12:00:00', '376109'),    
(10, 2007, 113, '2025-07-13', '13:00:00', '17:00:00', '659742');
UNLOCK TABLES;
