package com.Amaterasu.Main;
import Analysis.LL1Analysis;
import Analysis.NTerminalS;
import Analysis.Symbol;
import Analysis.TerminalS;
import Exceptions.CMMException;
import Lexer.Lexer;
import Lexer.Token;

import static Analysis.NTerminalS.*;
import static Analysis.TerminalS.*;
import java.util.*;
import java.io.*;

public class Main {

    LL1Analysis analysis;
    public Main() {
        analysis = new LL1Analysis();
        loadRules();
/*
        analysis.addRule(S, T, SX);
        analysis.addRule(SX, plus, T, SX);
        analysis.addRule(SX, nil);
        analysis.addRule(T, F, TX);
        analysis.addRule(TX, times, F, TX);
        analysis.addRule(TX, nil);
        analysis.addRule(F, i);
        analysis.addRule(F, LEFTP, S, RIGHTP);*/
        analysis.CalculateSets();

        Lexer lexer = new Lexer("input.cmm");
        ArrayList<Token> input = lexer.startParse();
        try {
            analysis.Analysis(input);
        } catch (CMMException e) {
            System.out.println(e);
        }
    }

    public boolean isTerminal(String str) {
        for (TerminalS ts : TerminalS.values()) {
            if (str.equals(ts.name()))
                return true;
        }
        return false;
    }

    public void loadRules() {
        File file = new File("rules.txt");
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String currentLine = null;
            while ((currentLine = reader.readLine()) != null) {
                String[] init = currentLine.split("->");
                NTerminalS left = NTerminalS.valueOf(init[0]);
                String[] rightStr = init[1].split(" ");
                Symbol[] right = new Symbol[rightStr.length];
                for (int i=0;i<rightStr.length;i++) {
                    if (isTerminal(rightStr[i]))
                        right[i] = TerminalS.valueOf(rightStr[i]);
                    else right[i] = NTerminalS.valueOf(rightStr[i]);
                }
                analysis.addRule(left, right);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
	// write your code here
        //new Lexer("input.cmm").startParse();
        new Main();
    }
}
