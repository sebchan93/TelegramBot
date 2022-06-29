package com.company;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class MySQLAccess {
    private Connection connect = null;
    private Statement statement = null;
    private PreparedStatement preparedStatement = null;
    private PreparedStatement preparedStatementOne = null;
    private PreparedStatement preparedStatementTwo = null;
    private PreparedStatement preparedStatementThree = null;
    private ResultSet resultSet = null;




    public boolean updateTrackingDetails (int formID, int messageTemplateID, int trackingID) throws Exception {
        try {
            // This will load the MySQL driver, each DB has its own driver
            Class.forName("com.mysql.jdbc.Driver");
            // Setup the connection with the DB
            connect = DriverManager
                    .getConnection("jdbc:mysql://localhost/telegram?"
                            + "user=sqluser&password=sqluserpw");

            // Statements allow to issue SQL queries to the database
            statement = connect.createStatement();

            preparedStatement = connect
                    .prepareStatement("Update telegram.tracking set ChatFormID = ? , ChatMessageTemplateID = ? WHERE TrackingID = ?;");
            preparedStatement.setInt(1, formID);
            preparedStatement.setInt(2, messageTemplateID);
            preparedStatement.setInt(3, trackingID);
            preparedStatement.executeUpdate();



            return true;
        } catch (Exception e) {
            throw e;
        } finally {
            close();
        }

    }


    public ChatFormDTO getFirstChatMessageTemplate (int chatFormID) throws Exception {
        try {
            // This will load the MySQL driver, each DB has its own driver
            Class.forName("com.mysql.jdbc.Driver");
            // Setup the connection with the DB
            connect = DriverManager
                    .getConnection("jdbc:mysql://localhost/telegram?"
                            + "user=sqluser&password=sqluserpw");

            // Statements allow to issue SQL queries to the database
            statement = connect.createStatement();

            preparedStatement = connect
                    .prepareStatement("SELECT * FROM telegram.chat_form_template inner join telegram.chat_message_template on telegram.chat_form_template.ChatFormID = telegram.chat_message_template.chatFormID where telegram.chat_form_template.ChatFormID = ? and telegram.chat_message_template.Position = '1' ;");
            preparedStatement.setInt(1, chatFormID);
            resultSet = preparedStatement.executeQuery();
            ChatFormDTO formDTO = null;
            while (resultSet.next()){
                // e.g. resultSet.getSTring(2);
                formDTO= new ChatFormDTO();
                //  int chatFormID = resultSet.getInt("ChatFormID");

                int chatMessageTemplateID = resultSet.getInt("ChatMessageTemplateID");
                String chatMessageTemplate = resultSet.getString("MessageTemplate");
                boolean replyRequired = resultSet.getBoolean("ReplyRequired");
                formDTO.setChatMessageTemplateID(chatMessageTemplateID);
                formDTO.setMessageTemplate(chatMessageTemplate);
                formDTO.setChatFormID(chatFormID);
                formDTO.setReplyRequired(replyRequired);

            }


            return formDTO;
        } catch (Exception e) {
            throw e;
        } finally {
            close();
        }

    }


    public ChatFormDTO getChatMessageTemplate (TrackingDTO trackingDTO ) throws Exception {
        try {
            // This will load the MySQL driver, each DB has its own driver
            Class.forName("com.mysql.jdbc.Driver");
            // Setup the connection with the DB
            connect = DriverManager
                    .getConnection("jdbc:mysql://localhost/telegram?"
                            + "user=sqluser&password=sqluserpw");

            // Statements allow to issue SQL queries to the database
            statement = connect.createStatement();

            preparedStatement = connect
                    .prepareStatement("SELECT * FROM telegram.chat_message_template where ChatMessageTemplateID = ? ;");
            preparedStatement.setInt(1, trackingDTO.getChatMessageTemplateID());
            resultSet = preparedStatement.executeQuery();
           int position = 0 ;
            ChatFormDTO formDTO = null;
           while (resultSet.next()) {
                 position =  resultSet.getInt("Position");

            }
            preparedStatementOne = connect
                    .prepareStatement("SELECT * FROM telegram.chat_message_template where ChatFormID = ? and Position = ? ;");
            preparedStatementOne.setInt(1, trackingDTO.getChatFormID());
            preparedStatementOne.setInt(2, position+1);
            resultSet = preparedStatementOne.executeQuery();
           if(resultSet.next() == false) {
               formDTO = new ChatFormDTO();
           } else {
               do {

                   int chatMessageTemplateID = resultSet.getInt("ChatMessageTemplateID");
                   int chatFormID = resultSet.getInt("ChatFormID");
                   boolean replyRequired = resultSet.getBoolean("ReplyRequired");
                   String chatMessageTemplate = resultSet.getString("MessageTemplate");
                   System.out.println(chatMessageTemplate);
                   formDTO = new ChatFormDTO();
                   formDTO.setChatMessageTemplateID(chatMessageTemplateID);
                   formDTO.setMessageTemplate(chatMessageTemplate);
                   formDTO.setReplyRequired(replyRequired);
                   formDTO.setChatFormID(chatFormID);
               }
               while (resultSet.next()) ;
           }

            preparedStatementTwo = connect
                    .prepareStatement("SELECT * FROM telegram.chat_message_template where ChatFormID = ? and Position = ? ;");
            preparedStatementTwo.setInt(1, trackingDTO.getChatFormID());
            preparedStatementTwo.setInt(2, position+2);
            resultSet = preparedStatementTwo.executeQuery();
            if(resultSet.next() == false) {
                updateFinalTrackingTemplate(trackingDTO.getTrackingID());
            }

            return formDTO;
        } catch (Exception e) {
            throw e;
        } finally {
            close();
        }

    }
    public boolean insertReply(TrackingDTO trackingDTO, String reply) throws Exception {
        try {
            // This will load the MySQL driver, each DB has its own driver
            Class.forName("com.mysql.jdbc.Driver");
            // Setup the connection with the DB
            connect = DriverManager
                    .getConnection("jdbc:mysql://localhost/telegram?" + "user=sqluser&password=sqluserpw");

            // Statements allow to issue SQL queries to the database
            statement = connect.createStatement();

            String key[] = { "ReplyID" };
            preparedStatement = connect
                        .prepareStatement("insert into  telegram.telegram_reply  (ReplyID, ChatMessageTemplateID, Reply, TelegramUserName,DateOfReply) values (default,?,?,?,?)", key);
            int chatMessageTemplateID = trackingDTO.getChatMessageTemplateID();
            String username = trackingDTO.getUserName();
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            String replyDate = now.format(dtf);
            preparedStatement.setInt(1, chatMessageTemplateID);
            preparedStatement.setString(2, reply);
            preparedStatement.setString(3, username);
            preparedStatement.setString(4, replyDate);
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            throw e;
        } finally {
            close();
        }
        return true;

    }


    public boolean insertMode(String username, String mode) throws Exception {
        try {
            // This will load the MySQL driver, each DB has its own driver
            Class.forName("com.mysql.jdbc.Driver");
            // Setup the connection with the DB
            connect = DriverManager
                    .getConnection("jdbc:mysql://localhost/telegram?" + "user=sqluser&password=sqluserpw");

            // Statements allow to issue SQL queries to the database
            statement = connect.createStatement();

            String key[] = { "UserID" };
            preparedStatement = connect
                    .prepareStatement("insert into  telegram.telegram_mode  (UserID, Username, Mode, Date) values (default,?,?,?)", key);
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            String date = now.format(dtf);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, mode);
            preparedStatement.setString(3, date);
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            throw e;
        } finally {
            close();
        }
        return true;

    }


    public boolean updateMode(String username, String mode, int faqTopicID) throws Exception {
        try {
            // This will load the MySQL driver, each DB has its own driver
            Class.forName("com.mysql.jdbc.Driver");
            // Setup the connection with the DB
            connect = DriverManager
                    .getConnection("jdbc:mysql://localhost/telegram?" + "user=sqluser&password=sqluserpw");

            // Statements allow to issue SQL queries to the database
            statement = connect.createStatement();

            String key[] = { "UserID" };
            preparedStatement = connect
                    .prepareStatement("update  telegram.telegram_mode  set Mode = ?, Date = ? , faqID = ? where Username = ? ;");
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            String date = now.format(dtf);
            preparedStatement.setString(1, mode);
            preparedStatement.setString(2, date);
            if(mode.equalsIgnoreCase("FORM KEY")){
            preparedStatement.setInt(3, faqTopicID);}
            else{preparedStatement.setNull(3,java.sql.Types.INTEGER);}
            preparedStatement.setString(4, username);
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            throw e;
        } finally {
            close();
        }
        return true;

    }



    public ModeDTO getTelegramMode(String username) throws Exception {
        ModeDTO dto = null;
        try {
            // This will load the MySQL driver, each DB has its own driver
            Class.forName("com.mysql.jdbc.Driver");
            // Setup the connection with the DB
            connect = DriverManager
                    .getConnection("jdbc:mysql://localhost/telegram?" + "user=sqluser&password=sqluserpw");

            // Statements allow to issue SQL queries to the database
            statement = connect.createStatement();
            preparedStatement = connect
                    .prepareStatement("Select * from  telegram.telegram_mode where Username = ? ;");
            preparedStatement.setString(1, username);
            resultSet =  preparedStatement.executeQuery();

            while (resultSet.next()){
                // e.g. resultSet.getSTring(2);
             dto = new ModeDTO();
                String user = resultSet.getString("Username");
                int faqTopicID = resultSet.getInt("faqID");
                String mode = resultSet.getString("Mode");
                dto.setUsername(user);
                dto.setMode(mode);
                dto.setFaqTopicID(faqTopicID);

            }
        } catch (Exception e) {
            throw e;
        } finally {
            close();
        }
        return dto;

    }

    public TrackingDTO getTrackingDetails (String userName) throws Exception {
        try {
            // This will load the MySQL driver, each DB has its own driver
            Class.forName("com.mysql.jdbc.Driver");
            // Setup the connection with the DB
            connect = DriverManager
                    .getConnection("jdbc:mysql://localhost/telegram?"
                            + "user=sqluser&password=sqluserpw");

            // Statements allow to issue SQL queries to the database
            statement = connect.createStatement();

            preparedStatement = connect
                    .prepareStatement("SELECT * from telegram.tracking  where Username = ? and Complete = 0 ");
            preparedStatement.setString(1, userName);
            resultSet = preparedStatement.executeQuery();
            TrackingDTO trackingDTO = null;
            while (resultSet.next()){
                // e.g. resultSet.getSTring(2);
                trackingDTO= new TrackingDTO();
                int trackingID = resultSet.getInt("TrackingID");
                //  String userName  = resultSet.getString("Username");
                int chatFormID = resultSet.getInt("ChatFormID");
                int chatMessageTemplateID = resultSet.getInt("ChatMessageTemplateID");
                boolean complete = resultSet.getBoolean("Complete");
                trackingDTO.setTrackingID(trackingID);
                trackingDTO.setUserName(userName);
                trackingDTO.setChatFormID(chatFormID);
                trackingDTO.setChatMessageTemplateID(chatMessageTemplateID);
                trackingDTO.setComplete(complete);
            }

            if(trackingDTO != null) {
                preparedStatementThree = connect
                        .prepareStatement("SELECT * from  telegram.chat_message_template where ChatMessageTemplateID = ? ;");
                preparedStatementThree.setInt(1, trackingDTO.getChatMessageTemplateID());
                resultSet = preparedStatementThree.executeQuery();
                while (resultSet.next()){

                    boolean replyRequired = resultSet.getBoolean("ReplyRequired");
                    trackingDTO.setReplyRequired(replyRequired);
                }

            }


            return trackingDTO;
        } catch (Exception e) {
            throw e;
        } finally {
            close();
        }

    }


    public boolean updateFinalTrackingTemplate (int trackingID) throws Exception {
        try {
            // This will load the MySQL driver, each DB has its own driver
            Class.forName("com.mysql.jdbc.Driver");
            // Setup the connection with the DB
            connect = DriverManager
                    .getConnection("jdbc:mysql://localhost/telegram?"
                            + "user=sqluser&password=sqluserpw");

            // Statements allow to issue SQL queries to the database
            statement = connect.createStatement();

            preparedStatement = connect
                    .prepareStatement("Update telegram.tracking set Complete = 1  WHERE TrackingID = ?;");
            preparedStatement.setInt(1, trackingID);
            preparedStatement.executeUpdate();



            return true;
        } catch (Exception e) {
            throw e;
        } finally {
            close();
        }

    }

    public boolean insertUserNameTracking(String userName) throws Exception {
        try {
            // This will load the MySQL driver, each DB has its own driver
            Class.forName("com.mysql.jdbc.Driver");
            // Setup the connection with the DB
            connect = DriverManager
                    .getConnection("jdbc:mysql://localhost/telegram?" + "user=sqluser&password=sqluserpw");

            // Statements allow to issue SQL queries to the database
            statement = connect.createStatement();

            String key[] = { "TrackingID" };
                preparedStatement = connect
                        .prepareStatement("insert into  telegram.tracking  (TrackingID, UserName, Complete) values (default,?,?)", key);

                preparedStatement.setString(1, userName);
            preparedStatement.setBoolean(2, false);
                preparedStatement.executeUpdate();
        } catch (Exception e) {
            throw e;
        } finally {
            close();
        }
        return true;

    }

    public String readDataBase() throws Exception {
        try {
            // This will load the MySQL driver, each DB has its own driver
            Class.forName("com.mysql.jdbc.Driver");
            // Setup the connection with the DB
            connect = DriverManager
                    .getConnection("jdbc:mysql://localhost/telegram?"
                            + "user=sqluser&password=sqluserpw");

            // Statements allow to issue SQL queries to the database
            statement = connect.createStatement();
            // Result set get the result of the SQL query
            resultSet = statement
                    .executeQuery("select * from telegram.comments");
            writeResultSet(resultSet);

            // PreparedStatements can use variables and are more efficient
            preparedStatement = connect
                    .prepareStatement("insert into  telegram.comments values (default, ?, ?, ?, ? , ?, ?)");
            // "myuser, webpage, datum, summary, COMMENTS from telegram.comments");
            // Parameters start with 1
            preparedStatement.setString(1, "Test");
            preparedStatement.setString(2, "TestEmail");
            preparedStatement.setString(3, "TestWebpage");
            preparedStatement.setDate(4, new java.sql.Date(2009, 12, 11));
            preparedStatement.setString(5, "TestSummary");
            preparedStatement.setString(6, "TestComment");
            preparedStatement.executeUpdate();

            preparedStatement = connect
                    .prepareStatement("SELECT myuser, webpage, datum, summary, COMMENTS from telegram.comments");
            resultSet = preparedStatement.executeQuery();
            String result =   writeResultSet(resultSet);

            // Remove again the insert comment
            preparedStatement = connect
                    .prepareStatement("delete from telegram.comments where myuser= ? ; ");
            preparedStatement.setString(1, "Test");
            preparedStatement.executeUpdate();

            resultSet = statement
                    .executeQuery("select * from telegram.comments");
          writeMetaData(resultSet);
             return result;
        } catch (Exception e) {
            throw e;
        } finally {
            close();
        }

    }

    private void writeMetaData(ResultSet resultSet) throws SQLException {
        //  Now get some metadata from the database
        // Result set get the result of the SQL query

        System.out.println("The columns in the table are: ");

        System.out.println("Table: " + resultSet.getMetaData().getTableName(1));
        for  (int i = 1; i<= resultSet.getMetaData().getColumnCount(); i++){
            System.out.println("Column " +i  + " "+ resultSet.getMetaData().getColumnName(i));
        }
    }

    private String writeResultSet(ResultSet resultSet) throws SQLException {
        // ResultSet is initially before the first data set
        String user = "";
        while (resultSet.next()) {
            // It is possible to get the columns via name
            // also possible to get the columns via the column number
            // which starts at 1
            // e.g. resultSet.getSTring(2);
             user = resultSet.getString("myuser");
            String website = resultSet.getString("webpage");
            String summary = resultSet.getString("summary");
            Date date = resultSet.getDate("datum");
            String comment = resultSet.getString("comments");
            System.out.println("User: " + user);
            System.out.println("Website: " + website);
            System.out.println("summary: " + summary);
            System.out.println("Date: " + date);
            System.out.println("Comment: " + comment);
        }
        return user;
    }

    // You need to close the resultSet
    private void close() {
        try {
            if (resultSet != null) {
                resultSet.close();
            }

            if (statement != null) {
                statement.close();
            }

            if (connect != null) {
                connect.close();
            }
        } catch (Exception e) {

        }
    }



    public Map<String,Integer> getFaqTopicDetails () throws Exception {
        try {
            // This will load the MySQL driver, each DB has its own driver
            Class.forName("com.mysql.jdbc.Driver");
            // Setup the connection with the DB
            connect = DriverManager
                    .getConnection("jdbc:mysql://localhost/telegram?"
                            + "user=sqluser&password=sqluserpw");

            // Statements allow to issue SQL queries to the database
            statement = connect.createStatement();

            String sql = "SELECT * "
                    + "from telegram.telegram_faq ;";

            preparedStatement = connect
                    .prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();
            Map<String,Integer> faqTopicMap = new HashMap<>();
            while (resultSet.next()){

                int faqID = resultSet.getInt("faqID");
                String faqTopicName = resultSet.getString("FaqTopicName");
                faqTopicMap.put(faqTopicName,faqID);
            }






            return faqTopicMap;
        } catch (Exception e) {
            throw e;
        } finally {
            close();
        }

    }

    public List<FaqDTO> getFaqKeywords (int faqTopic) throws Exception {
        try {
            // This will load the MySQL driver, each DB has its own driver
            Class.forName("com.mysql.jdbc.Driver");
            // Setup the connection with the DB
            connect = DriverManager
                    .getConnection("jdbc:mysql://localhost/telegram?"
                            + "user=sqluser&password=sqluserpw");

            // Statements allow to issue SQL queries to the database
            statement = connect.createStatement();


            preparedStatement = connect
                    .prepareStatement("SELECT * FROM  telegram_faq inner join faq_topics_template on  telegram_faq.faqID = faq_topics_template.faqID " +
                            " left join telegram.faq_keyword on faq_keyword.faqTopicID = faq_topics_template.faqTopicID " +
                            " where telegram_faq.faqID = ? ;");
            preparedStatement.setInt(1, faqTopic);
            resultSet = preparedStatement.executeQuery();
            List <FaqDTO> faqList = new ArrayList();
            while (resultSet.next()){
                FaqDTO dto= new FaqDTO();
                String keyword = resultSet.getString("faqKeyword");
                String faqQuestion = resultSet.getString("faqQuestion");
                String faqAnswer = resultSet.getString("faqAnswer");
                System.out.println(keyword);
                dto.setFaqKeyword(keyword);
                dto.setFaqQuestion(faqQuestion);
                dto.setFaqAnswer(faqAnswer);
                faqList.add(dto);
            }

            return faqList;
        } catch (Exception e) {
            throw e;
        } finally {
            close();
        }

    }

}