package com.barak.tabs.Parser;

import org.xml.sax.helpers.DefaultHandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.jar.Attributes;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by Barak Halevi on 08/10/2018.
 */
public class RssParser extends DefaultHandler
{
    private RssFeed       mRssFeed;
    private StringBuilder mLocalBuffer;
    private RssItem          mCurrentItem;
    private boolean       mImgStatus;

    private String mXmlContent;

    public RssParser(String xmlContent)
    {
        mXmlContent = xmlContent;
        mLocalBuffer = new StringBuilder();
    }

    public void parse()
    {
        SAXParserFactory spf = null;
        SAXParser sp = null;

        try
        {
            spf = SAXParserFactory.newInstance();
            if (spf != null)
            {
                sp = spf.newSAXParser();
                sp.parse(new ByteArrayInputStream(mXmlContent.getBytes()), this);
            }
        }
        /*
         * Exceptions need to be handled
         * MalformedURLException
         * ParserConfigurationException
         * IOException
         * SAXException
         */

        catch (Exception e)
        {
            System.out.println("Exception: " + e);
            e.printStackTrace();
        }
    }
    public RssFeed getFeed()
    {
        return (this.mRssFeed);
    }

    public void startElement(String uri, String localName, String qName,
                             Attributes attributes)
    {
        if (qName.equalsIgnoreCase("channel"))
            this.mRssFeed = new RssFeed();
        else if (qName.equalsIgnoreCase("item") && (this.mRssFeed != null))
        {
            this.mCurrentItem = new RssItem();
            this.mRssFeed.addItem(this.mCurrentItem);
        }
        else if (qName.equalsIgnoreCase("image") && (this.mRssFeed != null))
            this.mImgStatus = true;
    }

    public void endElement(String uri, String localName, String qName)
    {
        if (this.mRssFeed == null)
            return;

        if (qName.equalsIgnoreCase("item"))
            this.mCurrentItem = null;

        else if (qName.equalsIgnoreCase("image"))
            this.mImgStatus = false;

        else if (qName.equalsIgnoreCase("title"))
        {
            if (this.mCurrentItem != null) this.mCurrentItem.title = this.mLocalBuffer.toString().trim();
            else if (this.mImgStatus) this.mRssFeed.imageTitle = this.mLocalBuffer.toString().trim();
            else this.mRssFeed.title = this.mLocalBuffer.toString().trim();
        }

        else if (qName.equalsIgnoreCase("link"))
        {
            if (this.mCurrentItem != null) this.mCurrentItem.link = this.mLocalBuffer.toString().trim();
            else if (this.mImgStatus) this.mRssFeed.imageLink = this.mLocalBuffer.toString().trim();
            else this.mRssFeed.link = this.mLocalBuffer.toString().trim();
        }

        else if (qName.equalsIgnoreCase("description"))
        {
            if (this.mCurrentItem != null) this.mCurrentItem.description = this.mLocalBuffer.toString().trim();
            else this.mRssFeed.description = this.mLocalBuffer.toString().trim();
        }

        else if (qName.equalsIgnoreCase("url") && this.mImgStatus)
            this.mRssFeed.imageUrl = this.mLocalBuffer.toString().trim();

        else if (qName.equalsIgnoreCase("language"))
            this.mRssFeed.language = this.mLocalBuffer.toString().trim();

        else if (qName.equalsIgnoreCase("generator"))
            this.mRssFeed.generator = this.mLocalBuffer.toString().trim();

        else if (qName.equalsIgnoreCase("copyright"))
            this.mRssFeed.copyright = this.mLocalBuffer.toString().trim();

        else if (qName.equalsIgnoreCase("pubDate") && (this.mCurrentItem != null))
            this.mCurrentItem.pubDate = this.mLocalBuffer.toString().trim();

        else if (qName.equalsIgnoreCase("category") && (this.mCurrentItem != null))
            this.mRssFeed.addItem(this.mLocalBuffer.toString().trim(), this.mCurrentItem);

        this.mLocalBuffer.setLength(0);
    }

    public void characters(char[] ch, int start, int length)
    {
        this.mLocalBuffer.append(ch, start, length);
    }

    public static void _setProxy()
            throws IOException
    {
        Properties sysProperties = System.getProperties();
        sysProperties.put("proxyHost", "<Proxy IP Address>");
        sysProperties.put("proxyPort", "<Proxy Port Number>");
        System.setProperties(sysProperties);
    }

    public static class RssFeed
    {
        public  String title;
        public  String description;
        public  String link;
        public  String language;
        public  String generator;
        public  String copyright;
        public  String imageUrl;
        public  String imageTitle;
        public  String imageLink;

        private ArrayList <RssItem> items;
        private HashMap <String, ArrayList <RssItem>> category;

        public void addItem(RssItem item)
        {
            if (this.items == null)
                this.items = new ArrayList<RssItem>();
            this.items.add(item);
        }

        public void addItem(String category, RssItem item)
        {
            if (this.category == null)
                this.category = new HashMap<String, ArrayList<RssItem>>();
            if (!this.category.containsKey(category))
                this.category.put(category, new ArrayList<RssItem>());
            this.category.get(category).add(item);
        }

        public ArrayList <RssItem> getFeedItems()
        {
            return items;
        }
    }


    public static class RssItem
    {
        public  String title;
        public  String description;
        public  String link;
        public  String pubDate;

        public String toString()
        {
            return (this.title + ": " + this.pubDate + "n" + this.description);
        }
    }

}
