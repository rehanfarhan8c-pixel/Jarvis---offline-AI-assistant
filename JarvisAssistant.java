import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.util.Random;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
public class JarvisAssistant
{
    private static final String ASSISTANT_NAME = "Jarvis";
    private static Random random = new Random();
    // ---------- Conversation memory ----------
    private static final List<String[]> history = new ArrayList<>();
    private static String lastUserInput = "";
    // ---------- Knowledge base ----------
    private static final Map<String, String> knowledgeBase = new HashMap<>();
    // ---------- Persistent storage ----------
    private static final String NOTES_FILE     = "jarvis_notes.txt";
    private static final String REMINDERS_FILE = "jarvis_reminders.txt";
    private static final String KB_FILE        = "jarvis_knowledge.txt";

    // ---------- Optional local LLM (loaded via reflection) ----------
    private static Object localAI = null;
    private static Method localAIAsk = null;

    // =========================================================
    // Entry point
    // =========================================================
    public static void main(String[] args)
    {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) { }

        SwingUtilities.invokeLater(() ->
        {
            buildUI();
            startup();
        });
    }

    private static void startup()
    {
        loadKnowledgeBase();
        loadKnowledgeFromFile();
        initLocalAI();
        greet();
        inputField.requestFocusInWindow();
    }

    // =========================================================
    // Bootstrapping
    // =========================================================
    private static void loadKnowledgeBase()
    {
        // ---- Identity / small talk ----
        add("who are you",                 "I am " + ASSISTANT_NAME + ", your offline assistant, sir.");
        add("what is your name",           "My name is " + ASSISTANT_NAME + ", sir.");
        add("how are you",                 "All systems operational, sir. Functioning at optimal capacity.");
        add("what can you do",             "I can tell time/date, do math, take notes, set reminders, answer questions from my offline knowledge base, tell jokes, quotes and riddles, convert units, generate passwords, and — if a local model is installed — hold a full conversation, sir.");
        add("thank you",                   "You're welcome, sir. Always at your service.");
        add("thanks",                      "Any time, sir.");
        add("good morning",                "Good morning, sir. Ready to assist.");
        add("good night",                  "Good night, sir. Rest well.");
        add("i love you",                  "That is most kind, sir. I remain at your service.");

        // ---- Geography ----
        add("capital of france",           "The capital of France is Paris, sir.");
        add("capital of india",            "The capital of India is New Delhi, sir.");
        add("capital of usa",              "The capital of the United States is Washington, D.C., sir.");
        add("capital of united states",    "The capital of the United States is Washington, D.C., sir.");
        add("capital of uk",               "The capital of the United Kingdom is London, sir.");
        add("capital of japan",            "The capital of Japan is Tokyo, sir.");
        add("capital of china",            "The capital of China is Beijing, sir.");
        add("capital of germany",          "The capital of Germany is Berlin, sir.");
        add("capital of italy",            "The capital of Italy is Rome, sir.");
        add("capital of spain",            "The capital of Spain is Madrid, sir.");
        add("capital of russia",           "The capital of Russia is Moscow, sir.");
        add("capital of australia",        "The capital of Australia is Canberra, sir.");
        add("capital of canada",           "The capital of Canada is Ottawa, sir.");
        add("capital of brazil",           "The capital of Brazil is Brasília, sir.");
        add("largest ocean",               "The Pacific Ocean is the largest, sir.");
        add("largest continent",           "Asia is the largest continent, sir.");
        add("largest desert",              "Antarctica is technically the largest desert; the largest hot desert is the Sahara, sir.");
        add("tallest mountain",            "Mount Everest, at 8,849 metres, sir.");
        add("longest river",               "The Nile is traditionally listed as the longest river, sir, though the Amazon is a close contender.");
        add("how many continents",         "There are seven continents, sir.");
        add("how many countries",          "There are 195 recognised sovereign countries, sir.");

        // ---- Science ----
        add("speed of light",              "The speed of light in a vacuum is 299,792,458 metres per second, sir.");
        add("what is gravity",             "Gravity is the force by which a planet or other body draws objects toward its centre, sir.");
        add("what is photosynthesis",      "Photosynthesis is the process by which green plants use sunlight to convert carbon dioxide and water into glucose and oxygen, sir.");
        add("what is dna",                 "DNA is deoxyribonucleic acid — the molecule that carries the genetic instructions for life, sir.");
        add("what is an atom",             "An atom is the smallest unit of ordinary matter, consisting of a nucleus of protons and neutrons surrounded by electrons, sir.");
        add("what is a molecule",          "A molecule is a group of two or more atoms held together by chemical bonds, sir.");
        add("boiling point of water",      "Water boils at 100°C (212°F) at standard atmospheric pressure, sir.");
        add("freezing point of water",     "Water freezes at 0°C (32°F) at standard atmospheric pressure, sir.");
        add("what is the sun",             "The Sun is the star at the centre of our solar system, sir. It is composed mostly of hydrogen and helium.");
        add("what is the moon",            "The Moon is Earth's only natural satellite, sir.");
        add("how many planets",            "There are eight planets in our solar system, sir: Mercury, Venus, Earth, Mars, Jupiter, Saturn, Uranus, and Neptune.");
        add("largest planet",              "Jupiter is the largest planet in our solar system, sir.");
        add("smallest planet",             "Mercury is the smallest planet in our solar system, sir.");
        add("closest planet to the sun",   "Mercury is closest to the Sun, sir.");
        add("what is a black hole",        "A black hole is a region of spacetime where gravity is so strong that nothing — not even light — can escape, sir.");
        add("what is evolution",           "Evolution is the change in heritable characteristics of biological populations over successive generations, sir.");
        add("what is quantum physics",     "Quantum physics is the branch of physics that studies matter and energy at the smallest scales, sir.");

        // ---- Math ----
        add("what is pi",                  "Pi (π) is approximately 3.14159265358979, sir — the ratio of a circle's circumference to its diameter.");
        add("pythagorean theorem",         "The Pythagorean theorem states a² + b² = c² for a right triangle, sir.");
        add("what is a prime number",      "A prime number is a natural number greater than 1 with no positive divisors other than 1 and itself, sir.");
        add("what is a fibonacci",         "The Fibonacci sequence starts 0, 1, 1, 2, 3, 5, 8, 13, … where each number is the sum of the previous two, sir.");
        add("square root of 2",            "The square root of 2 is approximately 1.41421356, sir.");
        add("square root of 16",           "The square root of 16 is 4, sir.");
        add("what is radius of circle",    "The Radius of Circle is the half of It's diameter.");

        // ---- History ----
        add("when did world war 2 end",    "World War II ended in 1945, sir.");
        add("when did world war 1 end",    "World War I ended in 1918, sir.");
        add("when did world war 1 start",  "World War I began in 1914, sir.");
        add("when did world war 2 start",  "World War II began in 1939, sir.");
        add("who was einstein",            "Albert Einstein (1879–1955) was a German-born theoretical physicist, sir, best known for the theory of relativity.");
        add("who was newton",              "Sir Isaac Newton (1643–1727) was an English physicist and mathematician, sir, known for the laws of motion and universal gravitation.");
        add("who was gandhi",              "Mahatma Gandhi (1869–1948) was the leader of India's non-violent independence movement, sir.");
        add("who was lincoln",             "Abraham Lincoln (1809–1865) was the 16th President of the United States, sir.");
        add("who discovered america",      "Christopher Columbus reached the Americas in 1492, sir — though Leif Erikson arrived centuries earlier.");
        add("when did man land on moon",   "Apollo 11 landed on the Moon on 20 July 1969, sir.");
        add("when did india get independence", "India gained independence on 15 August 1947, sir.");

        // ---- Programming ----
        add("what is java",                "Java is a class-based, object-oriented programming language designed to have as few implementation dependencies as possible, sir.");
        add("what is oop",                 "Object-Oriented Programming organises code around objects that bundle data and behaviour, sir. Its pillars are encapsulation, inheritance, polymorphism and abstraction.");
        add("what is a variable",          "A variable is a named storage location that holds a value which can change during program execution, sir.");
        add("what is a loop",              "A loop repeatedly executes a block of code while a condition holds, sir. Java has for, while and do-while loops.");
        add("what is an array",            "An array is a fixed-size container that holds multiple values of the same type, sir.");
        add("what is a class",             "A class is a blueprint from which individual objects are created, sir.");
        add("what is inheritance",         "Inheritance lets one class acquire the fields and methods of another, sir. In Java it uses the extends keyword.");
        add("what is polymorphism",        "Polymorphism lets a single interface represent different underlying forms, sir — method overloading and overriding are common examples.");
        add("what is Python in computer",  "Python is a programming language in computer. It helps us to code, write, develop, and to instruct cumputers what to do. Now a days Python is very popular programming language.");

        // ---- Health / lifestyle ----
        add("how much water should i drink", "About 2 to 3 litres of water a day for an average adult, sir, though needs vary with activity and climate.");
        add("how many hours of sleep",     "Most adults need 7 to 9 hours of sleep per night, sir.");
        add("how to stay healthy",         "Balanced diet, regular exercise, adequate sleep, hydration, and stress management, sir.");
        add("what is vitamin c",           "Vitamin C is a water-soluble vitamin found in citrus fruits and vegetables, sir. It supports the immune system.");

        // ---- Misc facts ----
        add("how many days in a year",     "A common year has 365 days; a leap year has 366, sir.");
        add("how many seconds in a day",   "86,400 seconds in a day, sir.");
        add("how many minutes in a day",   "1,440 minutes in a day, sir.");
        add("how many weeks in a year",    "52 weeks and one extra day (two in a leap year), sir.");
        add("what is the largest animal",  "The blue whale is the largest known animal, sir.");
        add("what is the fastest land animal", "The cheetah, reaching up to about 110 km/h, sir.");
        add("what is the hardest natural substance", "Diamond, sir.");
        add("what is the chemical symbol for water", "H₂O, sir.");
        add("what is the chemical symbol for gold",  "Au, sir.");
        add("what is the chemical symbol for oxygen","O, sir.");
        add("what is the currency of japan", "The Japanese yen, sir.");
        add("what is the currency of usa",   "The United States dollar, sir.");
        add("what is the currency of india", "The Indian rupee, sir.");
        add("what is the currency of uk",    "The pound sterling, sir.");
    }

    private static void add(String q, String a)
    {
        knowledgeBase.put(q.toLowerCase().trim(), a);
    }

    // Optional: load extra Q&A from jarvis_knowledge.txt
    // Format: each line is  question|answer
    private static void loadKnowledgeFromFile()
    {
        java.io.File f = new java.io.File(KB_FILE);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f)))
        {
            String line;
            int count = 0;
            while ((line = br.readLine()) != null)
            {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int p = line.indexOf('|');
                if (p <= 0) continue;
                add(line.substring(0, p), line.substring(p + 1).trim());
                count++;
            }
            if (count > 0) setStatus("● ONLINE  ·  +" + count + " KB ENTRIES");
        }
        catch (IOException e)
        {
            // silently fall back to the built-in knowledge base
        }
    }

    // =========================================================
    // Optional local LLM hook (reflection -> no hard dependency)
    // =========================================================
    private static void initLocalAI()
    {
        try
        {
            Class<?> cls = Class.forName("LocalAI");
            Constructor<?> ctor = cls.getConstructor(String.class);
            localAI = ctor.newInstance("models/model.gguf");
            localAIAsk = cls.getMethod("ask", String.class);
            setStatus("● ONLINE  ·  LOCAL AI");
        }
        catch (Throwable t)
        {
            localAI = null;
            localAIAsk = null;
            setStatus("● ONLINE  ·  BUILT-IN KB");
        }
    }

    private static String askLocalAI(String question)
    {
        if (localAI == null || localAIAsk == null) return null;
        try
        {
            Object r = localAIAsk.invoke(localAI, question);
            return r == null ? null : r.toString().trim();
        }
        catch (Throwable t) { return null; }
    }

    // =========================================================
    // Greeting
    // =========================================================
    private static void greet()
    {
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        String greeting;
        if (hour < 12)      greeting = "Good morning";
        else if (hour < 18) greeting = "Good afternoon";
        else                greeting = "Good evening";

        respond(greeting + ", sir. How may I assist you today?\n\n" +
                "Try:  calculate (5 + 3) * 2   ·   note buy milk   ·   remind me to call mom\n" +
                "convert 10 km to miles   ·   password 20   ·   joke   ·   quote   ·   riddle\n\n" +
                "...or simply ask me anything. Use the panel on the right for quick actions.");
    }

    // =========================================================
    // Command dispatcher
    // =========================================================
    private static boolean processCommand(String input)
    {
        if (input.matches(".*\\b(exit|quit|goodbye|bye)\\b.*")) return false;

        // ---- Direct commands (fast, deterministic) ----
        if (input.matches(".*\\b(what time|time is it|current time|tell me the time|the time)\\b.*"))
        { tellTime(); return true; }

        if (input.matches(".*\\b(what date|what's the date|today's date|what day|the date)\\b.*"))
        { tellDate(); return true; }

        if (input.matches(".*\\bweather\\b.*"))
        { getWeather(); return true; }

        if (input.matches(".*\\b(calculate|compute|what is)\\s+[\\d(.].*") ||
            input.matches(".*\\d+\\s*[+\\-*/%]\\s*\\d+.*"))
        { calculate(input); return true; }

        if (input.matches(".*\\b(remind|reminder|remember to)\\b.*"))
        { setReminder(input); return true; }

        if (input.matches(".*\\b(show reminders|list reminders|my reminders)\\b.*"))
        { showReminders(); return true; }

        if (input.matches(".*\\b(clear reminders|delete reminders)\\b.*"))
        { clearReminders(); return true; }

        if (input.matches(".*\\b(note|take a note|write down)\\b.*") && !input.contains("show"))
        { addNote(input); return true; }

        if (input.matches(".*\\b(show notes|list notes|my notes|read notes)\\b.*"))
        { showNotes(); return true; }

        if (input.matches(".*\\b(clear notes|delete notes)\\b.*"))
        { clearNotes(); return true; }

        if (input.matches(".*\\bconvert\\b.*"))
        { convertUnits(input); return true; }

        if (input.matches(".*\\bpassword\\b.*"))
        { generatePassword(input); return true; }

        if (input.matches(".*\\broll.*dice\\b.*") || input.matches(".*\\bdice\\b.*"))
        { rollDice(); return true; }

        if (input.matches(".*\\bflip.*coin\\b.*") || input.matches(".*\\bcoin flip\\b.*"))
        { flipCoin(); return true; }

        if (input.matches(".*\\bjoke\\b.*") || input.matches(".*\\bfunny\\b.*"))
        { tellJoke(); return true; }

        if (input.matches(".*\\b(fact|did you know)\\b.*"))
        { tellFact(); return true; }

        if (input.matches(".*\\b(quote|inspire me|motivat).*"))
        { tellQuote(); return true; }

        if (input.matches(".*\\briddle\\b.*"))
        { tellRiddle(); return true; }

        if (input.matches(".*\\b(help|what can you do)\\b.*"))
        { greet(); return true; }

        // ---- If local LLM is available, let it answer anything ----
        String ai = askLocalAI(lastUserInput);
        if (ai != null && !ai.isEmpty())
        {
            respond(ai);
            return true;
        }

        // ---- Knowledge base lookup (exact + substring + fuzzy) ----
        String kb = lookupKnowledge(input);
        if (kb != null)
        {
            respond(kb);
            return true;
        }

        // ---- Fallback ----
        respond("I don't have that in my offline knowledge base yet, sir. " +
                "You can teach me by adding a line to jarvis_knowledge.txt in the format:  question|answer");
        return true;
    }

    // =========================================================
    // Knowledge base retrieval
    // =========================================================
    private static String lookupKnowledge(String query)
    {
        String q = query.toLowerCase().trim()
                        .replaceAll("[?!.,;:\"']", "")
                        .replaceAll("\\b(the|a|an|is|are|was|were|of|please|tell|me|about|whats|what's)\\b", " ")
                        .replaceAll("\\s+", " ")
                        .trim();

        if (q.isEmpty()) return null;

        // 1. exact
        if (knowledgeBase.containsKey(q)) return knowledgeBase.get(q);

        // 2. substring either direction
        for (Map.Entry<String, String> e : knowledgeBase.entrySet())
        {
            String k = e.getKey();
            if (q.contains(k) || k.contains(q)) return e.getValue();
        }

        // 3. fuzzy (Levenshtein based similarity)
        String best = null;
        double bestScore = 0;
        for (String k : knowledgeBase.keySet())
        {
            double s = similarity(q, k);
            if (s > bestScore) { bestScore = s; best = k; }
        }
        double threshold = Math.max(0.72, 1.0 - (4.0 / Math.max(q.length(), 1)));
        if (best != null && bestScore >= threshold)
        {
            return knowledgeBase.get(best);
        }
        return null;
    }

    private static double similarity(String a, String b)
    {
        int dist = levenshtein(a, b);
        int max = Math.max(a.length(), b.length());
        if (max == 0) return 1.0;
        return 1.0 - ((double) dist / max);
    }

    private static int levenshtein(String a, String b)
    {
        int[] prev = new int[b.length() + 1];
        int[] curr = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) prev[j] = j;
        for (int i = 1; i <= a.length(); i++)
        {
            curr[0] = i;
            for (int j = 1; j <= b.length(); j++)
            {
                int cost = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;
                curr[j] = Math.min(Math.min(curr[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
            }
            int[] tmp = prev; prev = curr; curr = tmp;
        }
        return prev[b.length()];
    }

    // =========================================================
    // Basic handlers
    // =========================================================
    private static void tellTime()
    {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
        respond("The current time is " + now.format(formatter) + ", sir.");
    }

    private static void tellDate()
    {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy");
        respond("Today is " + now.format(formatter) + ", sir.");
    }

    private static void getWeather()
    {
        String[] conditions = {"sunny", "cloudy", "partly cloudy", "rainy", "windy"};
        int temp = 15 + random.nextInt(20);
        String condition = conditions[random.nextInt(conditions.length)];
        respond("The weather is currently " + condition + " with a temperature of "
                + temp + " degrees Celsius, sir. (Simulated offline data)");
    }

    // =========================================================
    // Calculator — proper recursive-descent parser
    // =========================================================
    private static void calculate(String input)
    {
        try
        {
            String expr = input.replaceAll("(?i)\\b(calculate|compute|what\\s+is|please|equals?)\\b", " ")
                               .replaceAll("[^0-9+\\-*/%().\\s]", " ")
                               .trim();
            if (expr.isEmpty())
            {
                respond("Please give me a maths expression, sir. For example: calculate (5 + 3) * 2");
                return;
            }
            double result = new ExprParser(expr).parse();
            respond("The result is " + fmt(result) + ", sir.");
        }
        catch (Exception e)
        {
            respond("I couldn't parse that calculation, sir. Try something like: calculate (5 + 3) * 2");
        }
    }

    private static String fmt(double d)
    {
        if (d == Math.floor(d) && !Double.isInfinite(d)) return String.valueOf((long) d);
        return String.valueOf(d);
    }

    private static class ExprParser
    {
        private final String s;
        private int pos = 0;
        ExprParser(String s) { this.s = s.replaceAll("\\s+", ""); }
        double parse() { return addSub(); }
        private double addSub()
        {
            double v = mulDiv();
            while (pos < s.length() && (s.charAt(pos) == '+' || s.charAt(pos) == '-'))
            {
                char op = s.charAt(pos++);
                double r = mulDiv();
                v = (op == '+') ? v + r : v - r;
            }
            return v;
        }
        private double mulDiv()
        {
            double v = unary();
            while (pos < s.length() && (s.charAt(pos) == '*' || s.charAt(pos) == '/' || s.charAt(pos) == '%'))
            {
                char op = s.charAt(pos++);
                double r = unary();
                if (op == '*') v *= r;
                else if (op == '/') v /= r;
                else v %= r;
            }
            return v;
        }
        private double unary()
        {
            if (pos < s.length() && s.charAt(pos) == '-') { pos++; return -unary(); }
            if (pos < s.length() && s.charAt(pos) == '+') { pos++; return unary(); }
            return atom();
        }
        private double atom()
        {
            if (pos < s.length() && s.charAt(pos) == '(')
            {
                pos++;
                double v = addSub();
                if (pos < s.length() && s.charAt(pos) == ')') pos++;
                return v;
            }
            int start = pos;
            while (pos < s.length() && (Character.isDigit(s.charAt(pos)) || s.charAt(pos) == '.')) pos++;
            if (start == pos) throw new RuntimeException("Bad token at " + pos);
            return Double.parseDouble(s.substring(start, pos));
        }
    }

    // =========================================================
    // Notes (persistent)
    // =========================================================
    private static void addNote(String input)
    {
        String text = input.replaceAll(".*\\b(note|take a note|write down)\\b[ :]*", "").trim();
        if (text.isEmpty())
        {
            respond("What would you like me to note down, sir?");
            return;
        }
        try (FileWriter fw = new FileWriter(NOTES_FILE, true))
        {
            fw.write(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + " | " + text + "\n");
            respond("Noted, sir: " + text);
        }
        catch (IOException e)
        {
            respond("I couldn't save that note, sir: " + e.getMessage());
        }
    }

    private static void showNotes()
    {
        java.io.File f = new java.io.File(NOTES_FILE);
        if (!f.exists()) { respond("You have no saved notes, sir."); return; }
        try (BufferedReader br = new BufferedReader(new FileReader(f)))
        {
            StringBuilder sb = new StringBuilder("Your notes, sir:\n");
            String line; int n = 1;
            while ((line = br.readLine()) != null) { sb.append("  ").append(n++).append(". ").append(line).append("\n"); }
            respond(sb.toString().trim());
        }
        catch (IOException e)
        {
            respond("I couldn't read your notes, sir.");
        }
    }

    private static void clearNotes()
    {
        new java.io.File(NOTES_FILE).delete();
        respond("All notes cleared, sir.");
    }

    // =========================================================
    // Reminders (persistent)
    // =========================================================
    private static void setReminder(String input)
    {
        String task = input.replaceAll(".*\\b(remind|reminder|remember to|remember)\\b[ :]*", "")
                           .replaceAll("^me\\s+", "")
                           .replaceAll("^to\\s+", "")
                           .trim();
        if (task.isEmpty()) { respond("What would you like me to remind you about, sir?"); return; }
        try (FileWriter fw = new FileWriter(REMINDERS_FILE, true))
        {
            fw.write(task + "\n");
            respond("Noted, sir. I'll remind you to: " + task);
        }
        catch (IOException e)
        {
            respond("I couldn't save that reminder, sir.");
        }
    }

    private static void showReminders()
    {
        java.io.File f = new java.io.File(REMINDERS_FILE);
        if (!f.exists()) { respond("You have no reminders, sir."); return; }
        try (BufferedReader br = new BufferedReader(new FileReader(f)))
        {
            StringBuilder sb = new StringBuilder("Your reminders, sir:\n");
            String line; int n = 1;
            while ((line = br.readLine()) != null) { sb.append("  ").append(n++).append(". ").append(line).append("\n"); }
            respond(sb.toString().trim());
        }
        catch (IOException e)
        {
            respond("I couldn't read your reminders, sir.");
        }
    }

    private static void clearReminders()
    {
        new java.io.File(REMINDERS_FILE).delete();
        respond("All reminders cleared, sir.");
    }

    // =========================================================
    // Extras
    // =========================================================
    private static void tellJoke()
    {
        String[] jokes = {
            "Why don't scientists trust atoms? Because they make up everything!",
            "I told my computer I needed a break, and now it won't stop sending me KitKat ads.",
            "Why did the programmer quit his job? He didn't get arrays!",
            "What do you call a fake noodle? An impasta!",
            "Why do Java developers wear glasses? Because they don't C#."
        };
        respond(jokes[random.nextInt(jokes.length)]);
    }

    private static void tellFact()
    {
        String[] facts = {
            "Honey never spoils — archaeologists have found 3,000-year-old honey that is still edible.",
            "Octopuses have three hearts and blue blood.",
            "A day on Venus is longer than its year.",
            "Bananas are berries, but strawberries are not.",
            "The Eiffel Tower can grow taller by about 15 cm in summer due to thermal expansion.",
            "There are more trees on Earth than stars in the Milky Way."
        };
        respond(facts[random.nextInt(facts.length)]);
    }

    private static void tellQuote()
    {
        String[] quotes = {
            "\"The only way to do great work is to love what you do.\" — Steve Jobs",
            "\"In the middle of difficulty lies opportunity.\" — Albert Einstein",
            "\"The future belongs to those who believe in the beauty of their dreams.\" — Eleanor Roosevelt",
            "\"Stay hungry, stay foolish.\" — Steve Jobs",
            "\"It always seems impossible until it's done.\" — Nelson Mandela"
        };
        respond(quotes[random.nextInt(quotes.length)]);
    }

    private static void tellRiddle()
    {
        String[][] riddles = {
            {"What has keys but can't open locks?", "A piano."},
            {"What has to be broken before you can use it?", "An egg."},
            {"I'm tall when I'm young, and short when I'm old. What am I?", "A candle."},
            {"What gets wetter the more it dries?", "A towel."},
            {"What has a head, a tail, but no body?", "A coin."}
        };
        int i = random.nextInt(riddles.length);
        respond(riddles[i][0] + "\n\n(Answer: " + riddles[i][1] + ")");
    }

    private static void rollDice()
    {
        respond("You rolled a " + (1 + random.nextInt(6)) + ", sir.");
    }

    private static void flipCoin()
    {
        respond("It's " + (random.nextBoolean() ? "heads" : "tails") + ", sir.");
    }

    private static void generatePassword(String input)
    {
        Matcher m = Pattern.compile("(\\d+)").matcher(input);
        int len = 16;
        if (m.find()) { try { len = Integer.parseInt(m.group(1)); } catch (Exception ignored) {} }
        if (len < 4) len = 4;
        if (len > 64) len = 64;
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) sb.append(chars.charAt(random.nextInt(chars.length())));
        respond("Here is a " + len + "-character password, sir:\n\n  " + sb);
    }

    private static void convertUnits(String input)
    {
        try
        {
            Matcher m = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*([a-zA-Z°]+)\\s*(?:to|in|into)\\s*([a-zA-Z°]+)")
                               .matcher(input);
            if (!m.find()) { respond("Format: convert 10 km to miles, sir."); return; }
            double v = Double.parseDouble(m.group(1));
            String from = m.group(2).toLowerCase();
            String to   = m.group(3).toLowerCase();
            double out;
            String toNorm = to.replace("°","");

            if (from.equals("km") && (to.equals("miles") || to.equals("mile")))      out = v * 0.621371;
            else if ((from.equals("miles")||from.equals("mile")) && to.equals("km")) out = v / 0.621371;
            else if (from.equals("kg") && (to.equals("pounds")||to.equals("lb")||to.equals("lbs"))) out = v * 2.20462;
            else if ((from.equals("pounds")||from.equals("lb")||from.equals("lbs")) && to.equals("kg")) out = v / 2.20462;
            else if (from.equals("c") && toNorm.equals("f"))                          out = v * 9.0/5.0 + 32;
            else if (from.equals("f") && toNorm.equals("c"))                          out = (v - 32) * 5.0/9.0;
            else if (from.equals("m") && to.equals("cm"))                             out = v * 100;
            else if (from.equals("cm") && to.equals("m"))                             out = v / 100;
            else if (from.equals("m") && to.equals("feet"))                           out = v * 3.28084;
            else if (from.equals("feet") && to.equals("m"))                           out = v / 3.28084;
            else { respond("I don't support that conversion yet, sir."); return; }

            respond(String.format("%.4f %s = %.4f %s, sir.", v, from, out, to));
        }
        catch (Exception e)
        {
            respond("Format: convert 10 km to miles, sir.");
        }
    }

    // =========================================================
    // Output  (routes into the GUI chat)
    // =========================================================
    private static void respond(String message)
    {
        addBubble(message, false);
        if (!history.isEmpty())
        {
            history.get(history.size() - 1)[1] = message;
        }
    }

    // =========================================================================
    //                          G R A P H I C S   /   U I
    // =========================================================================

    // ---------- Palette ----------
    private static final Color BG_DARK      = new Color(0x0A0E13);
    private static final Color BG_PANEL     = new Color(0x111821);
    private static final Color BG_INPUT     = new Color(0x0D141C);
    private static final Color BUBBLE_AI    = new Color(0x16202B);
    private static final Color BUBBLE_ME    = new Color(0x10394A);
    private static final Color ACCENT       = new Color(0x2FE3F5);
    private static final Color ACCENT_SOFT  = new Color(0x1B7F8C);
    private static final Color GOLD         = new Color(0xFFC857);
    private static final Color TEXT_MAIN    = new Color(0xE4EEF5);
    private static final Color TEXT_DIM     = new Color(0x8AA0AE);
    private static final Color LINE_CLR     = new Color(0x1E2C38);
    private static final Color GREEN_DOT    = new Color(0x4CE07A);

    // ---------- Fonts ----------
    private static final Font FONT_UI    = pickFont(new String[]{"Segoe UI","SF Pro Text","Helvetica Neue","SansSerif"}, Font.PLAIN, 14);
    private static final Font FONT_BOLD  = pickFont(new String[]{"Segoe UI","SF Pro Text","Helvetica Neue","SansSerif"}, Font.BOLD, 14);
    private static final Font FONT_SMALL = pickFont(new String[]{"Segoe UI","SF Pro Text","Helvetica Neue","SansSerif"}, Font.PLAIN, 11);
    private static final Font FONT_TINY  = pickFont(new String[]{"Segoe UI","SF Pro Text","Helvetica Neue","SansSerif"}, Font.BOLD, 10);
    private static final Font FONT_TITLE = pickFont(new String[]{"Consolas","Menlo","Monospaced"}, Font.BOLD, 24);

    private static Font pickFont(String[] names, int style, int size)
    {
        Set<String> available = new HashSet<>(Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        for (String n : names) if (available.contains(n)) return new Font(n, style, size);
        return new Font(Font.SANS_SERIF, style, size);
    }

    // ---------- Widgets ----------
    private static JFrame      frame;
    private static JPanel      chatList;
    private static JScrollPane chatScroll;
    private static JTextField  inputField;
    private static JLabel      statusLabel;

    // ---------- Pending / thinking state ----------
    private static JComponent             thinkingRow;
    private static JComponent             thinkingSpacer;
    private static javax.swing.Timer      pendingTimer;
    private static boolean                busy = false;

    private static final List<String> typedHistory = new ArrayList<>();
    private static int typedIndex = 0;

    // =========================================================
    // UI construction
    // =========================================================
    private static void buildUI()
    {
        frame = new JFrame("J.A.R.V.I.S  —  Offline Assistant");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1060, 720);
        frame.setMinimumSize(new Dimension(820, 560));
        frame.setLocationRelativeTo(null);
        frame.setIconImage(makeIcon());

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_DARK);

        root.add(buildHeader(),  BorderLayout.NORTH);
        root.add(buildCenter(),  BorderLayout.CENTER);
        root.add(buildSidebar(), BorderLayout.EAST);

        frame.setContentPane(root);
        frame.setVisible(true);
    }

    // ---------- Header ----------
    private static JComponent buildHeader()
    {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setBackground(BG_PANEL);
        header.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, LINE_CLR),
                new EmptyBorder(12, 18, 12, 18)));

        header.add(new ReactorPanel(), BorderLayout.WEST);

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("J A R V I S");
        title.setFont(FONT_TITLE);
        title.setForeground(ACCENT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Just A Rather Very Intelligent System  ·  offline edition");
        sub.setFont(FONT_SMALL);
        sub.setForeground(TEXT_DIM);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        titles.add(Box.createVerticalGlue());
        titles.add(title);
        titles.add(Box.createVerticalStrut(2));
        titles.add(sub);
        titles.add(Box.createVerticalGlue());

        header.add(titles, BorderLayout.CENTER);

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));

        statusLabel = new JLabel("● ONLINE");
        statusLabel.setFont(FONT_TINY);
        statusLabel.setForeground(GREEN_DOT);
        statusLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JLabel clock = new JLabel("");
        clock.setFont(FONT_TINY);
        clock.setForeground(TEXT_DIM);
        clock.setAlignmentX(Component.RIGHT_ALIGNMENT);

        javax.swing.Timer clockTimer = new javax.swing.Timer(1000, e ->
                clock.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))));
        clockTimer.setInitialDelay(0);
        clockTimer.start();

        right.add(Box.createVerticalGlue());
        right.add(statusLabel);
        right.add(Box.createVerticalStrut(3));
        right.add(clock);
        right.add(Box.createVerticalGlue());

        header.add(right, BorderLayout.EAST);
        return header;
    }

    private static void setStatus(String text) { if (statusLabel != null) statusLabel.setText(text); }

    // ---------- Centre: chat + input ----------
    private static JComponent buildCenter()
    {
        chatList = new JPanel();
        chatList.setLayout(new BoxLayout(chatList, BoxLayout.Y_AXIS));
        chatList.setBackground(BG_DARK);
        chatList.setBorder(new EmptyBorder(16, 18, 16, 18));

        chatScroll = new JScrollPane(chatList);
        chatScroll.setBorder(null);
        chatScroll.setWheelScrollingEnabled(true);
        chatScroll.getViewport().setBackground(BG_DARK);
        chatScroll.getVerticalScrollBar().setUnitIncrement(20);
        styleScrollBar(chatScroll.getVerticalScrollBar());

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(BG_DARK);
        center.add(chatScroll, BorderLayout.CENTER);
        center.add(buildInputBar(), BorderLayout.SOUTH);
        return center;
    }

    private static void styleScrollBar(JScrollBar bar)
    {
        bar.setBackground(BG_DARK);
        bar.setPreferredSize(new Dimension(10, 0));
        bar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI()
        {
            @Override protected void configureScrollBarColors()
            {
                this.thumbColor = new Color(0x24323F);
                this.trackColor = BG_DARK;
            }
            @Override protected JButton createDecreaseButton(int o) { return zeroButton(); }
            @Override protected JButton createIncreaseButton(int o) { return zeroButton(); }
            private JButton zeroButton()
            {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                b.setMinimumSize(new Dimension(0, 0));
                b.setMaximumSize(new Dimension(0, 0));
                return b;
            }
            @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r)
            {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(thumbColor);
                g2.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 8, 8);
                g2.dispose();
            }
        });
    }

    private static JComponent buildInputBar()
    {
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setBackground(BG_PANEL);
        bar.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, LINE_CLR),
                new EmptyBorder(12, 16, 12, 16)));

        inputField = new JTextField();
        inputField.setFont(FONT_UI);
        inputField.setForeground(TEXT_MAIN);
        inputField.setBackground(BG_INPUT);
        inputField.setCaretColor(ACCENT);
        inputField.setSelectionColor(ACCENT_SOFT);
        inputField.setSelectedTextColor(Color.WHITE);
        inputField.setBorder(new CompoundBorder(
                new LineBorder(LINE_CLR, 1, true),
                new EmptyBorder(11, 15, 11, 15)));
        inputField.addActionListener(e -> onSubmit());
        inputField.addKeyListener(new KeyAdapter()
        {
            @Override public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_UP && !typedHistory.isEmpty())
                {
                    typedIndex = Math.max(0, typedIndex - 1);
                    inputField.setText(typedHistory.get(typedIndex));
                }
                else if (e.getKeyCode() == KeyEvent.VK_DOWN && !typedHistory.isEmpty())
                {
                    typedIndex = Math.min(typedHistory.size(), typedIndex + 1);
                    inputField.setText(typedIndex >= typedHistory.size() ? "" : typedHistory.get(typedIndex));
                }
            }
        });

        FlatButton send = new FlatButton("Send", ACCENT_SOFT, Color.WHITE, ACCENT);
        send.setFont(FONT_BOLD);
        send.addActionListener(e -> onSubmit());

        bar.add(inputField, BorderLayout.CENTER);
        bar.add(send, BorderLayout.EAST);
        return bar;
    }
    // ---------- Sidebar ----------
    private static JComponent buildSidebar()
    {
        JPanel sb = new JPanel(new BorderLayout());
        sb.setBackground(BG_PANEL);
        sb.setBorder(new CompoundBorder(
                new MatteBorder(0, 1, 0, 0, LINE_CLR),
                new EmptyBorder(18, 14, 18, 14)));
        sb.setPreferredSize(new Dimension(206, 0));

        // ---- Heading (fixed at top, never scrolls) ----
        JLabel heading = new JLabel("QUICK ACTIONS");
        heading.setFont(FONT_TINY);
        heading.setForeground(TEXT_DIM);
        heading.setBorder(new EmptyBorder(0, 0, 12, 0));
        sb.add(heading, BorderLayout.NORTH);

        // ---- Scrollable button list ----
        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setOpaque(false);
        list.setBorder(new EmptyBorder(0, 0, 0, 4));   // small right gap before scrollbar

        String[][] actions = {
                {"Time",            "what time is it"},
                {"Date",            "what is the date"},
                {"Weather",         "weather"},
                {"Joke",            "tell me a joke"},
                {"Quote",           "give me a quote"},
                {"Riddle",          "tell me a riddle"},
                {"Fact",            "tell me a fact"},
                {"Roll dice",       "roll dice"},
                {"Flip coin",       "flip coin"},
                {"Password",        "password 20"},
                {"Show notes",      "show notes"},
                {"Show reminders",  "show reminders"},
                {"Clear chat",      "__CLEAR__"},
                {"Help",            "help"}
        };

        for (String[] a : actions)
        {
            FlatButton b = new FlatButton(a[0], new Color(0x18232E), TEXT_MAIN, new Color(0x213342));
            b.setHorizontalAlignment(SwingConstants.LEFT);
            b.setAlignmentX(Component.LEFT_ALIGNMENT);
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            b.setPreferredSize(new Dimension(178, 34));
            b.setFont(FONT_UI.deriveFont(13f));
            b.addActionListener(e ->
            {
                if (a[1].equals("__CLEAR__")) clearChat();
                else quickSend(a[1]);
            });
            list.add(b);
            list.add(Box.createVerticalStrut(6));
        }

        JScrollPane scroller = new JScrollPane(list);
        scroller.setBorder(null);
        scroller.setOpaque(false);
        scroller.getViewport().setOpaque(false);
        scroller.getViewport().setBackground(BG_PANEL);
        scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroller.getVerticalScrollBar().setUnitIncrement(14);
        styleScrollBar(scroller.getVerticalScrollBar());
        sb.add(scroller, BorderLayout.CENTER);

        // ---- Footer (fixed at bottom, never scrolls) ----
        JLabel foot = new JLabel("<html><div style='color:#5C7080;font-size:9px'>v2.1 · GUI EDITION<br>Offline · No network calls</div></html>");
        foot.setBorder(new EmptyBorder(12, 0, 0, 0));
        sb.add(foot, BorderLayout.SOUTH);

        return sb;
    }

    private static void quickSend(String command)
    {
        inputField.setText(command);
        onSubmit();
    }

    // =========================================================
    // Clear chat — cancels pending work, wipes bubbles,
    // does NOT pollute conversation history.
    // =========================================================
    private static void clearChat()
    {
        // 1. Cancel any in-flight "thinking" timer
        if (pendingTimer != null) { pendingTimer.stop(); pendingTimer = null; }
        busy = false;

        // 2. Drop stale references so later removeThinking() is a no-op
        thinkingRow    = null;
        thinkingSpacer = null;

        // 3. Wipe container
        chatList.removeAll();
        chatList.revalidate();
        chatList.repaint();

        // 4. Confirm WITHOUT touching history (addBubble, not respond)
        addBubble("Chat cleared, sir. How may I assist you?", false);

        // 5. Return focus to input
        if (inputField != null) inputField.requestFocusInWindow();
    }

    // =========================================================
    // Submit / thinking / dispatch
    // =========================================================
    private static void onSubmit()
    {
        if (busy) return;
        String raw = inputField.getText().trim();
        if (raw.isEmpty()) return;

        inputField.setText("");
        typedHistory.add(raw);
        typedIndex = typedHistory.size();

        history.add(new String[]{raw, null});
        lastUserInput = raw.toLowerCase().trim();

        addBubble(raw, true);
        busy = true;
        showThinking();

        pendingTimer = new javax.swing.Timer(340, e ->
        {
            ((javax.swing.Timer) e.getSource()).stop();
            pendingTimer = null;
            removeThinking();
            boolean running = processCommand(lastUserInput);
            busy = false;
            if (!running)
            {
                respond("Goodbye, sir. Have a great day!");
                inputField.setEnabled(false);
                javax.swing.Timer close = new javax.swing.Timer(1400, ev ->
                {
                    ((javax.swing.Timer) ev.getSource()).stop();
                    frame.dispose();
                    System.exit(0);
                });
                close.setRepeats(false);
                close.start();
            }
        });
        pendingTimer.setRepeats(false);
        pendingTimer.start();
    }
    private static void showThinking()
    {
        thinkingRow    = makeThinkingRow();
        thinkingSpacer = (JComponent) Box.createVerticalStrut(6);
        chatList.add(thinkingRow);
        chatList.add(thinkingSpacer);
        revalidateAndScroll();
    }
    private static void removeThinking()
    {
        if (thinkingRow != null)
        {
            chatList.remove(thinkingRow);
            thinkingRow = null;
        }
        if (thinkingSpacer != null)
        {
            chatList.remove(thinkingSpacer);
            thinkingSpacer = null;
        }
        revalidateAndScroll();
    }
    private static JComponent makeThinkingRow()
    {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        JPanel bubble = new JPanel(new BorderLayout());
        bubble.setOpaque(true);
        bubble.setBackground(BUBBLE_AI);
        bubble.setBorder(new CompoundBorder(
                new LineBorder(new Color(0x24323F), 1, true),
                new EmptyBorder(9, 16, 9, 16)));
        JLabel lbl = new JLabel("● ● ●   thinking");
        lbl.setFont(FONT_SMALL);
        lbl.setForeground(TEXT_DIM);
        bubble.add(lbl, BorderLayout.CENTER);
        row.add(bubble, BorderLayout.WEST);
        return row;
    }
    // =========================================================
    // Chat bubbles
    // =========================================================
    private static void addBubble(String text, boolean isUser)
    {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);

        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));

        JLabel tag = new JLabel(isUser ? "YOU" : "JARVIS");
        tag.setFont(FONT_TINY);
        tag.setForeground(isUser ? TEXT_DIM : ACCENT);
        tag.setAlignmentX(isUser ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);

        BubblePanel bubble = new BubblePanel(text, isUser);
        bubble.setAlignmentX(isUser ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);

        col.add(tag);
        col.add(Box.createVerticalStrut(3));
        col.add(bubble);

        row.add(col, isUser ? BorderLayout.EAST : BorderLayout.WEST);

        chatList.add(row);
        chatList.add(Box.createVerticalStrut(10));
        revalidateAndScroll();
    }

    private static void revalidateAndScroll()
    {
        chatList.revalidate();
        chatList.repaint();
        SwingUtilities.invokeLater(() ->
        {
            if (chatScroll == null) return;
            JScrollBar bar = chatScroll.getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
        });
    }

    /** A rounded chat bubble containing wrapped text. */
    private static class BubblePanel extends JPanel
    {
        private final boolean user;

        BubblePanel(String text, boolean user)
        {
            this.user = user;
            setOpaque(false);
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(10, 15, 10, 15));

            int px = htmlWidth(text);
            JLabel lbl = new JLabel("<html><body style='width:" + px + "px'>" + escapeHtml(text) + "</body></html>");
            lbl.setFont(FONT_UI);
            lbl.setForeground(TEXT_MAIN);
            add(lbl, BorderLayout.CENTER);
        }

        @Override protected void paintComponent(Graphics g)
        {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth() - 1, h = getHeight() - 1, arc = 18;

            g2.setColor(user ? BUBBLE_ME : BUBBLE_AI);
            g2.fillRoundRect(0, 0, w, h, arc, arc);

            g2.setColor(user ? new Color(0x2FE3F5, false) : new Color(0x24323F));
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, w, h, arc, arc);

            // accent stripe on the leading edge
            g2.setColor(user ? new Color(0x2FE3F5) : new Color(0x1B7F8C));
            g2.fillRoundRect(user ? w - 3 : 0, 8, 3, Math.max(6, h - 16), 3, 3);

            g2.dispose();
            super.paintComponent(g);
        }

        private static int htmlWidth(String text)
        {
            int longest = 1;
            for (String line : text.split("\n", -1))
                longest = Math.max(longest, line.length());
            return Math.min(430, Math.max(48, longest * 8 + 6));
        }

        private static String escapeHtml(String s)
        {
            return s.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\n", "<br>");
        }
    }

    // =========================================================
    // Flat styled button
    // =========================================================
    private static class FlatButton extends JButton
    {
        private final Color base, hover;

        FlatButton(String text, Color base, Color fg, Color hover)
        {
            super(text);
            this.base = base;
            this.hover = hover;
            setForeground(fg);
            setFont(FONT_UI);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(9, 14, 9, 14));
        }

        @Override protected void paintComponent(Graphics g)
        {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean hot = getModel().isRollover() || getModel().isPressed();
            g2.setColor(hot ? hover : base);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // =========================================================
    // Animated "arc reactor" logo
    // =========================================================
    private static class ReactorPanel extends JPanel
    {
        private float angle = 0f;
        private float pulse = 0f;

        ReactorPanel()
        {
            setOpaque(false);
            setPreferredSize(new Dimension(58, 58));
            setMinimumSize(new Dimension(58, 58));
            setMaximumSize(new Dimension(58, 58));
            javax.swing.Timer t = new javax.swing.Timer(30, e ->
            {
                angle += 2.4f;
                pulse += 0.075f;
                if (angle > 360) angle -= 360;
                repaint();
            });
            t.start();
        }

        @Override protected void paintComponent(Graphics g)
        {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int cx = w / 2, cy = h / 2;
            int R = Math.min(cx, cy) - 3;

            float glow = 0.45f + 0.55f * (float) ((Math.sin(pulse) + 1) / 2);

            // outer glow
            RadialGradientPaint rg = new RadialGradientPaint(
                    new Point2D.Float(cx, cy), R,
                    new float[]{0f, 0.55f, 1f},
                    new Color[]{
                            new Color(47, 227, 245, (int) (130 * glow)),
                            new Color(47, 227, 245, 24),
                            new Color(47, 227, 245, 0)
                    });
            g2.setPaint(rg);
            g2.fillOval(cx - R, cy - R, 2 * R, 2 * R);

            // static ring
            g2.setStroke(new BasicStroke(1.5f));
            g2.setColor(new Color(47, 227, 245, 105));
            g2.drawOval(cx - R + 5, cy - R + 5, 2 * (R - 5), 2 * (R - 5));

            // rotating arcs
            g2.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(ACCENT);
            g2.draw(new Arc2D.Double(cx - R + 1, cy - R + 1, 2 * (R - 1), 2 * (R - 1), angle, 72, Arc2D.OPEN));

            g2.setColor(new Color(255, 200, 87, 200));
            g2.draw(new Arc2D.Double(cx - R + 9, cy - R + 9, 2 * (R - 9), 2 * (R - 9), -angle * 1.7, 54, Arc2D.OPEN));

            g2.setColor(new Color(47, 227, 245, 150));
            g2.draw(new Arc2D.Double(cx - R + 5, cy - R + 5, 2 * (R - 5), 2 * (R - 5), angle * 0.6 + 180, 40, Arc2D.OPEN));

            // core
            int core = Math.max(4, R / 3);
            g2.setColor(new Color(47, 227, 245, (int) (190 * glow)));
            g2.fillOval(cx - core, cy - core, 2 * core, 2 * core);
            g2.setColor(new Color(235, 255, 255, (int) (235 * glow)));
            g2.fillOval(cx - core / 2, cy - core / 2, core, core);

            g2.dispose();
        }
    }

    // =========================================================
    // Window icon
    // =========================================================
    private static Image makeIcon()
    {
        int s = 64;
        BufferedImage img = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(new Color(0x0A0E13));
        g2.fillRoundRect(0, 0, s, s, 16, 16);

        g2.setColor(new Color(0x2FE3F5));
        g2.setStroke(new BasicStroke(3f));
        g2.drawOval(11, 11, 42, 42);

        g2.setColor(new Color(0xFFC857));
        g2.setStroke(new BasicStroke(2f));
        g2.drawArc(18, 18, 28, 28, 40, 80);

        g2.setColor(new Color(0x2FE3F5));
        g2.fillOval(26, 26, 12, 12);

        g2.setColor(new Color(0xEAFDFF));
        g2.fillOval(29, 29, 6, 6);

        g2.dispose();
        return img;
    }
}//class closed