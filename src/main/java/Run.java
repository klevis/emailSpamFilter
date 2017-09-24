import org.tartarus.snowball.ext.PorterStemmer;

import java.util.StringTokenizer;

/**
 * Created by klevis.ramo on 9/22/2017.
 */
public class Run {

    static String email = "From merchantsworld2001@juno.com  Tue Aug  6 11:01:33 2002\n" +
            "Return-Path: <merchantsworld2001@juno.com>\n" +
            "Delivered-To: yyyy@localhost.netnoteinc.com\n" +
            "Received: from localhost (localhost [127.0.0.1])\n" +
            "\tby phobos.labs.netnoteinc.com (Postfix) with ESMTP id 8399C44126\n" +
            "\tfor <jm@localhost>; Tue,  6 Aug 2002 05:55:17 -0400 (EDT)\n" +
            "Received: from mail.webnote.net [193.120.211.219]\n" +
            "\tby localhost with POP3 (fetchmail-5.9.0)\n" +
            "\tfor jm@localhost (single-drop); Tue, 06 Aug 2002 10:55:17 +0100 (IST)\n" +
            "Received: from ns1.snaapp.com (066.dsl6660167.bstatic.surewest.net [66.60.167.66])\n" +
            "\tby webnote.net (8.9.3/8.9.3) with ESMTP id BAA09623\n" +
            "\tfor <jm@netnoteinc.com>; Sun, 4 Aug 2002 01:37:55 +0100\n" +
            "Message-Id: <200208040037.BAA09623@webnote.net>\n" +
            "Received: from html ([199.35.244.221]) by ns1.snaapp.com\n" +
            "          (Post.Office MTA v3.1.2 release (PO205-101c)\n" +
            "          ID# 0-47762U100L2S100) with SMTP id ABD354;\n" +
            "          Sat, 3 Aug 2002 17:32:59 -0700\n" +
            "From: yyyy@pluriproj.pt\n" +
            "Reply-To: merchantsworld2001@juno.com\n" +
            "To: yyyy@pluriproj.pt\n" +
            "Subject: Never Repay Cash Grants, $500 - $50,000, Secret Revealed!\n" +
            "Date: Sun, 19 Oct 1980 10:55:16\n" +
            "Mime-Version: 1.0\n" +
            "Content-Type: text/html; charset=\"DEFAULT\"\n" +
            "\n" +
            "<html><xbody>\n" +
            "http://klevis/com, !kaliaty.:''" +
            "<hr width = \"100%\">\n" +
            "<center><h3><font color =\n" +
            "\"#44C300\"><b>Government Grants E-Book 2002\n" +
            "edition, Just $15.95. Summer Sale, Good Until August 10, 2002!  Was $49.95.</font></b><p>\n" +
            "<table><Tr><td>\n" +
            "<li>You Can Receive The <font color =\n" +
            "\"green\"><b>Money</b></font> You Need...\n" +
            "<li>Every day <b><font color = \"green\">millions of\n" +
            "dollars</font></b> are given away to people, just like\n" +
            "you!!\n" +
            "<li>Your Government spends <b><font color =\n" +
            "\"green\">billions</font></b> of tax dollars on\n" +
            "government grants.\n" +
            "<li>Do you know that private foundations, trust and\n" +
            "corporations are\n" +
            "<li>required to give away a portion of theirs assets.\n" +
            "It doesn't matter,\n" +
            "<li>where you live (USA ONLY), your employment status,\n" +
            "or if you are broke, retired\n" +
            "<li>or living on a fixed income. There may be a grant\n" +
            "for you!\n" +
            "<hr width = \"100%\">\n" +
            "<li><font color = \"red\"><b>ANYONE</b></font> can apply\n" +
            "for a Grant from 18 years old and up!\n" +
            "<li>We will show you HOW & WHERE to get Grants. <font\n" +
            "color = \"red\"><b>THIS BOOK IS NEWLY UPDATED WITH THE\n" +
            "MOST CURRENT INFORMATION!!!</b></font>\n" +
            "<li>Grants from $500.00 to $50,000.00 are possible!\n" +
            "<li>GRANTS don't have to be paid back, EVER!\n" +
            "<li>Grants can be ideal for people who are or were\n" +
            "bankrupt or just have bad credit.\n" +
            "</td></tr></table>\n" +
            "<br><font size = \"+1\">Please Visit Our\n" +
            "Website<p></font>\n" +
            "And Place Your <font color = \"red\"> <b>Order\n" +
            "TODAY!</b> </font><a target=\"_blank\"  href =\n" +
            "\"http://www.geocities.com/grantzone_2002/\" ><b><font\n" +
            "size=\"5\">CLICK HERE</font></b> </a><p>&nbsp;<p>\n" +
            "\n" +
            "To Order by postal mail, please send $15.95 Plus $4.00 S & H. <br>\n" +
            "Make payable to <b>Grant Gold 2002</b>.<br>\n" +
            "<br>\n" +
            "</center>\n" +
            "Grant Gold 2002<br>\n" +
            "P. O. Box 36<br>\n" +
            "Dayton, Ohio  45405<br>\n" +
            "<br>\n" +
            "If you would like to order via Fax, please include your credit card information below and fax to our Fax Line.<br>\n" +
            "OUR 24 HOUR FAX NUMBER:  775-257-6657.\n" +
            "<br><br>\n" +
            "*****\n" +
            "Important Credit Card Information! Please Read Below!<br><br>\n" +
            " \n" +
            "*     Credit Card Address, City, State and Zip Code, must match billing address to be processed. \n" +
            "<br><br>\n" +
            "\n" +
            "CHECK____  MONEYORDER____  VISA____ MASTERCARD____ AmericanExpress___ Debt Card___\n" +
            "<br><br>\n" +
            "Name_______________________________________________________<br>\n" +
            "(As it appears on Check or Credit Card)<br>\n" +
            "<br>\n" +
            "Address____________________________________________________<br>\n" +
            "(As it appears on Check or Credit Card)<br>\n" +
            "<br>\n" +
            "___________________________________________________<br>\n" +
            "City,State,Zip(As it appears on Check or Credit Card)<br>\n" +
            "<br>\n" +
            "___________________________________________________<br>\n" +
            "(Credit Card Number)<br>\n" +
            "<br>\n" +
            "Expiration Month_____  Year_____<br>\n" +
            "<br>\n" +
            "___________________________________________________<br>\n" +
            "Email Address (Please Write Neat)<br>\n" +
            "<br>\n" +
            "___________________________________________________<br>\n" +
            "Authorized Signature\n" +
            "<p>\n" +
            "<font size=\"1\">\n" +
            "We apologize for any email you may have inadvertently\n" +
            "received.<br>\n" +
            "Please <a target=\"_blank\"  href =\n" +
            "\"http://www.mysoftwarefouruu.com/remove.htm\" >CLICK\n" +
            "HERE</a> to unsubscribe from future\n" +
            "mailings.</font><br>\n" +
            "\n" +
            "</BODY>\n" +
            "</HTML>\n" +
            "\n".toLowerCase();

    public static void main(String[] args) {

        String dollarReplaced = preapreEmail();
        tokenizeIntoWords(dollarReplaced);



    }

    private static void tokenizeIntoWords(String dollarReplaced) {
        String delim = "[' @$/#.-:&*+=[]?!(){},''\\\">_<;%'\t\n\r\f";
        System.out.println("delim = " + delim);
        StringTokenizer stringTokenizer = new StringTokenizer(dollarReplaced, delim);
        while (stringTokenizer.hasMoreElements()) {
            String word = (String) stringTokenizer.nextElement();
            String nonAlphaNumericRemoved = word.replaceAll("[^a-zA-Z0-9]", "");
            PorterStemmer stemmer = new PorterStemmer();
            stemmer.setCurrent(nonAlphaNumericRemoved);
            stemmer.stem();
            String stemmed = stemmer.getCurrent();
            System.out.println("" + stemmed);
        }
    }

    private static String preapreEmail() {
        String withoutHeader = email.substring(email.indexOf("\n\n"), email.length());
        String tagsRemoved = withoutHeader.replaceAll("<[^<>]+>", "");
        String numberedReplaced = tagsRemoved.replaceAll("[0-9]+", "XNUMBERX ");
        String urlReplaced = numberedReplaced.replaceAll("(http|https)://[^\\s]*", "XURLX ");
        String emailReplaced = urlReplaced.replaceAll("[^\\s]+@[^\\s]+", "XEMAILX ");
        String dollarReplaced = emailReplaced.replaceAll("[$]+", "XMONEYX ");
        System.out.println("dollarReplaced = " + dollarReplaced);
        return dollarReplaced;
    }


}
