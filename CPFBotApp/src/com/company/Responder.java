package com.company;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.apache.commons.text.similarity.CosineDistance;

import java.sql.DriverManager;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Responder extends TelegramLongPollingBot {

    MySQLAccess ms = new MySQLAccess();

    @Override
    public String getBotUsername() {
        return Bot.USERNAME;
    }

    @Override
    public String getBotToken() {
        return Bot.BOT_TOKEN;
    }

    @Override
    public void onUpdateReceived(Update update) {
        String response = "I'm Sorry. Please key in the correct unique ID.";
        String chatId = "";
        List<ChatFormDTO> chatList = new ArrayList<>();
       TrackingDTO trackingDTO = null;
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(response);
      sendMessage = homePage(sendMessage);



        if (update.hasCallbackQuery() && update.getCallbackQuery().getData() != null && !update.getCallbackQuery().getData().isEmpty()) {
            chatId = String.valueOf(update.getCallbackQuery().getMessage().getChatId());

            String callBackData = update.getCallbackQuery().getData();
            String username = update.getCallbackQuery().getFrom().getUserName();
            if (callBackData.equalsIgnoreCase(CallBackData.CD_ONE.toString())) {

                try {
                    if (username != null && !username.isEmpty()) {
                        ModeDTO dto = ms.getTelegramMode(username);
                        if(dto == null) {
                            ms.insertMode(username,"FAQ");
                            Map<String,Integer> map = ms.getFaqTopicDetails();
                            String faqTopics = getFaqTopicString(map);
                            sendMessage.setText("Welcome to FAQ. Please key in FAQ topics to start:\n" + faqTopics);
                        } else if ((dto.getMode().equalsIgnoreCase("FORM") || dto.getMode().equalsIgnoreCase("FORM KEY"))&& dto.getUsername().equalsIgnoreCase(username)){
                            ms.updateMode(username,"FAQ",0);
                            Map<String,Integer> map = ms.getFaqTopicDetails();
                            String faqTopics = getFaqTopicString(map);
                            sendMessage.setText("Switched to FAQ. Please key in FAQ topics to start:\n" + faqTopics);
                        } else if (dto.getMode().equalsIgnoreCase("FAQ") && dto.getUsername().equalsIgnoreCase(username)){
                            Map<String,Integer> map = ms.getFaqTopicDetails();
                            String faqTopics = getFaqTopicString(map);
                            sendMessage.setText("Already in FAQ. Please key in FAQ topics to start:\n" + faqTopics);

                        }
                    }
                }
                catch(Exception e){
                    e.printStackTrace();
                }


            }

            if (callBackData.equalsIgnoreCase(CallBackData.CD_TWO.toString())) {
                try {
                    if (username != null && !username.isEmpty()) {
                        ModeDTO dto = ms.getTelegramMode(username);
                        if(dto == null) {
                            ms.insertMode(username,"FORM");
                            sendMessage.setText("Welcome to CPF Bot Form. Please key in unique ID to start chat!");
                        } else if ((dto.getMode().equalsIgnoreCase("FAQ") || dto.getMode().equalsIgnoreCase("FORM KEY"))&& dto.getUsername().equalsIgnoreCase(username)){
                            ms.updateMode(username,"FORM",0);
                            sendMessage.setText("Switched to CPF Bot Form. Please key in unique ID to start chat!");
                        } else if (dto.getMode().equalsIgnoreCase("FORM") && dto.getUsername().equalsIgnoreCase(username)){
                            sendMessage.setText("Already in CPF Bot Form. Please key in unique ID to start chat!");

                        }
                    }
                    }
                 catch(Exception e){
                e.printStackTrace();
            }
            }
/*
            if (callBackData.equalsIgnoreCase(CallBackData.CD_FOUR.toString())) {
                sendMessage.setText("CCB.\n\n \n\n 1. Check for kkj \n\n 2. Book your jiao \n\n 3. Amend your lan jiao");
                faqQuery(sendMessage);
            }
            if (callBackData.equalsIgnoreCase(CallBackData.CD_A.toString())) {
                sendMessage.setText("OK CB.");
            }

            if (callBackData.equalsIgnoreCase(CallBackData.CD_B.toString())) {
                sendMessage.setText("Fine, LJ.");
            }
            if (callBackData.equalsIgnoreCase(CallBackData.CD_C.toString())) {
                sendMessage.setText("LJ KIA.");
            }
            if (callBackData.equalsIgnoreCase(CallBackData.CD_HOME.toString())) {
                homePage(sendMessage);
            }*/
        } else {
            chatId = String.valueOf(update.getMessage().getChatId());
            sendMessage.setText(response);
        }
        if (update.hasMessage() && update.getMessage().hasText()) {
            chatId = String.valueOf(update.getMessage().getChatId());
            String userName = update.getMessage().getFrom().getUserName();
            String userMessage = update.getMessage().getText().trim();
            ChatFormDTO formDTO = null;
            ModeDTO dto = null;

            if (userMessage != null && !userMessage.isEmpty()) {

                try {
                    dto = ms.getTelegramMode(userName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (dto == null) {
                    Map<String, Integer> map = null;
                    try {
                        map = ms.getFaqTopicDetails();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    String faqTopics = getFaqTopicString(map);
                    sendMessage.setText("Welcome to FAQ. Please key in FAQ topics to start:\n" + faqTopics);
                    try {
                        ms.insertMode(userName, "FAQ");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }


                //search tracking table if user has incomplete form
                else if (dto.getMode().equalsIgnoreCase("FORM")) {
                    // sendMessage = formProcessing(trackingDTO,userName,formDTO,userMessage,sendMessage);

                    try {
                        trackingDTO = ms.getTrackingDetails(userName);
                        // if no record
                        if (trackingDTO == null) {
                            sendMessage.setText("Please key in unique ID to start chat!");
                            ms.insertUserNameTracking(userName);
                            // print out ask for unique ID
                            //insert into tracking table
                        } else if (trackingDTO.getUserName() != null && !trackingDTO.getUserName().isEmpty()) {
                            if (trackingDTO.getChatFormID() == 0) {
                                if (isNumeric(userMessage)) {
                                    int formID = Integer.parseInt(userMessage);
                                    try {
                                        formDTO = ms.getFirstChatMessageTemplate(formID);
                                        if (formDTO != null && formDTO.getMessageTemplate() != null & !formDTO.getMessageTemplate().isEmpty()) {
                                            sendMessage.setText(formDTO.getMessageTemplate());
                                            ms.updateTrackingDetails(formID, formDTO.getChatMessageTemplateID(), trackingDTO.getTrackingID());
                                        } else {
                                            errorUniqueIDPage(sendMessage);

                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    errorUniqueIDPage(sendMessage);
                                }
                            } else {
                                formDTO = ms.getChatMessageTemplate(trackingDTO);
                                if (formDTO != null && formDTO.getMessageTemplate() != null && !formDTO.getMessageTemplate().isEmpty()) {
                                    sendMessage.setText(formDTO.getMessageTemplate());
                                    ms.updateTrackingDetails(formDTO.getChatFormID(), formDTO.getChatMessageTemplateID(), trackingDTO.getTrackingID());
                                    if (trackingDTO.isReplyRequired() == true) {
                                        ms.insertReply(trackingDTO, userMessage);

                                    }
                                }

                            }
                            // else if user record has chatformID null
                            // check if chatform id is integer and check if ID is valid
                            //if not valid, print key in correct ID
                            //else update tracking ID with chatform ID, chatmessage template ID of position 1
                            //print out first chat message template text
                        } else if (trackingDTO.getChatFormID() != 0 && trackingDTO.isComplete() == false) {
                            // chatform id not null and tracking table not complete
                            // get chatformID, chatmessagetemplateID and insert reply message to reply table (if not last position and if reply required)
                            formDTO = ms.getChatMessageTemplate(trackingDTO);
                            if (formDTO != null && formDTO.getMessageTemplate() != null && !formDTO.getMessageTemplate().isEmpty()) {
                                sendMessage.setText(formDTO.getMessageTemplate());
                                ms.updateTrackingDetails(formDTO.getChatFormID(), formDTO.getChatMessageTemplateID(), trackingDTO.getTrackingID());
                                if (trackingDTO.isReplyRequired() == true) {
                                    ms.insertReply(trackingDTO, userMessage);

                                }
                            }
                            // update tracking table with new chatmessage template ID to position + 1
                            // if last position then insert reply message and update tracking table with complete
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else if (dto.getMode().equalsIgnoreCase("FAQ")) {
                    try {
                        //  errorFaqPage(sendMessage);
                        Map<String, Integer> map = ms.getFaqTopicDetails();
                        ;
                        String faqTopics = getFaqTopicString(map);
                        String newMessage = userMessage.replaceFirst("/","");
                        if (map.containsKey(userMessage)) {
                            Integer faqTopicID = map.get(userMessage);
                            ms.updateMode(userName, "FORM KEY", faqTopicID);
                            sendMessage.setText("Please key in any queries on " + userMessage);
                        } else if (map.containsKey(newMessage)) {
                            Integer faqTopicID = map.get(newMessage);
                            ms.updateMode(userName, "FORM KEY", faqTopicID);
                            sendMessage.setText("Please key in any queries on " + newMessage);

                        } else
                        {
                            sendMessage.setText("I am sorry! I don't understand this FAQ topic! Please choose\n" + faqTopics);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (dto.getMode().equalsIgnoreCase("FORM KEY")) {
                   List<FaqDTO> faqList = null;
                    boolean flag = false;
                    try {
                        faqList   =   ms.getFaqKeywords(dto.getFaqTopicID());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    String reply = "";
                    TreeMap<Double, FaqDTO> sortedAnswer = new TreeMap<>();
                    for(int i = 0 ; i< faqList.size() && flag==false ; i++  ) {
                       String  question =   faqList.get(i).getFaqQuestion();
                        double cosineDistance = new CosineDistance().apply(question, userMessage);
                     double percentage =   Math.round((1 - cosineDistance) * 100);
                        if(percentage == 100.0){
                         reply = faqList.get(i).getFaqAnswer();
                            break;
                        } else if (percentage >= 50.0){
                            sortedAnswer.put(percentage,faqList.get(i));
                        }
                    }
                    if(!reply.isEmpty()) {

                        sendMessage.setText(reply);
                    } else if(!sortedAnswer.isEmpty())
                    {
                    Map.Entry value =     sortedAnswer.lastEntry();
                    FaqDTO valueAnswer = (FaqDTO) value.getValue();
                        sendMessage.setText("Your query is "+value.getKey()+ "% similar to "+ valueAnswer.getFaqQuestion()+"\n"+valueAnswer.getFaqAnswer());

                    } else{
                       String replyKeyword = "";
                        for(int i = 0 ; i< faqList.size() ; i++  ) {
                            String keyword = faqList.get(i).getFaqKeyword();
                           if(keyword.equalsIgnoreCase(userMessage)) {
                               replyKeyword = faqList.get(i).getFaqAnswer();
                            break;
                           }

                        }
                        if(!replyKeyword.isEmpty()) {

                            sendMessage.setText(replyKeyword);
                        }
                        else {
                            sendMessage.setText("I am sorry! I don't understand this query! You may try another query or Click FAQ to head back to list of topics and CPF BOT for forms");

                        }
                    }
             /*       double cosineDistance = new CosineDistance().apply(gravityNasa, gravityCambridge);

                    sendMessage.setText("I am sorry! I don't understand this query! You may try another query or Click FAQ to head back to list of topics and CPF BOT for forms");
*/
                }

            }

/*
            if (!userMessage.isEmpty()) {
                if(isNumeric(userMessage)) {
                    int formID = Integer.parseInt(userMessage);

                    System.out.println(formID);
                    try {
                        chatList =   ms.getChatFormDetail(formID);
                        for(ChatFormDTO e : chatList) {
                            e.getMessageTemplate();

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    errorUniqueIDPage(sendMessage);
                }

            }*/


                if (update.getMessage().hasContact()) {
                    sendMessage.setText("Thank you for sending us your phone number. We will contact you shortly!");

                    String phoneNumber = update.getMessage().getContact().getPhoneNumber().trim();
                    //You can now encrypt the phone number and store it.
                    System.out.println(phoneNumber);
                } else {
                    System.out.println("no details");

                }


                System.out.println(update.getMessage().toString());
            }


            if (chatId.isEmpty()) {
                throw new IllegalStateException("The chat id couldn't be identified or found.");
            }

            sendMessage.setChatId(chatId);
            try {
                sendApiMethod(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }

    }

    public SendMessage homePage (SendMessage sendMessage) {
      //  sendMessage.setText("Hi There, I'm your CPF bot. \n\n Please key in unique ID to start chat!");
        //    sendMessage.setText("Hi There, I'm your friendly retard bot. How may I help you today? \n\n 1. Check for availability \n\n 2. Book your lan sai \n\n 3. Amend your lan sai \n\n 4. FAQ your lan sai");

        // First create the keyboard
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        //Then we create the buttons' row
        List<InlineKeyboardButton> buttonsRow = new ArrayList<>();
        InlineKeyboardButton oneButton = new InlineKeyboardButton();
        oneButton.setText("FAQ");
        oneButton.setCallbackData(CallBackData.CD_ONE.toString());

        InlineKeyboardButton twoButton = new InlineKeyboardButton();
        twoButton.setText("CPF BOT");
        twoButton.setCallbackData(CallBackData.CD_TWO.toString());
/*
        InlineKeyboardButton threeButton = new InlineKeyboardButton();
        threeButton.setText("3");
        threeButton.setCallbackData(CallBackData.CD_THREE.toString());

        InlineKeyboardButton fourButton = new InlineKeyboardButton();
        fourButton.setText("4");
        fourButton.setCallbackData(CallBackData.CD_FOUR.toString());*/

        //We add the yes button to the buttons row
        buttonsRow.add(oneButton);
        buttonsRow.add(twoButton);
  //      buttonsRow.add(threeButton);
   //     buttonsRow.add(fourButton);


        //We add the newly created buttons row that contains the yes button to the keyboard
        keyboard.add(buttonsRow);

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(keyboard);
       sendMessage.setReplyMarkup(inlineKeyboardMarkup);

        return sendMessage;
    }



    public SendMessage errorUniqueIDPage (SendMessage sendMessage) {

        sendMessage.setText("I'm sorry. Please key in a unique ID to start the chat!");
      //  homePage(sendMessage);
        return sendMessage;
    }

    public SendMessage errorFaqPage (SendMessage sendMessage) {

        sendMessage.setText("I'm sorry. I don't understand the FAQ query!");
        //  homePage(sendMessage);
        return sendMessage;
    }

    public SendMessage faqQuery (SendMessage sendMessage) {

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        //Then we create the buttons' row
        List<InlineKeyboardButton> buttonsRow = new ArrayList<>();
        List<InlineKeyboardButton> homeRow = new ArrayList<>();

        InlineKeyboardButton oneButton = new InlineKeyboardButton();
        oneButton.setText("1");
        oneButton.setCallbackData(CallBackData.CD_A.toString());

        InlineKeyboardButton twoButton = new InlineKeyboardButton();
        twoButton.setText("2");
        twoButton.setCallbackData(CallBackData.CD_B.toString());

        InlineKeyboardButton threeButton = new InlineKeyboardButton();
        threeButton.setText("3");
        threeButton.setCallbackData(CallBackData.CD_C.toString());

        InlineKeyboardButton homeButton = new InlineKeyboardButton();
        homeButton.setText("Home");
        homeButton.setCallbackData(CallBackData.CD_HOME.toString());

        //We add the yes button to the buttons row
        buttonsRow.add(oneButton);
        buttonsRow.add(twoButton);
        buttonsRow.add(threeButton);

        homeRow.add(homeButton);
        //We add the newly created buttons row that contains the yes button to the keyboard
        keyboard.add(buttonsRow);
        keyboard.add(homeRow);
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(keyboard);

        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        return sendMessage;
    }


    private  boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }



    private String getFaqTopicString( Map<String,Integer> map){
        String topicName = "";
        Integer count = 1 ;
        for (Map.Entry<String,Integer> entry : map.entrySet()) {
           topicName = topicName.concat("/" + entry.getKey() +"\n");
            count++;
        }
        return topicName;
    }


}
