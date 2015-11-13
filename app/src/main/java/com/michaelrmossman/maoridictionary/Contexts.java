package com.michaelrmossman.maoridictionary;

public class Contexts {
    public static String getContext(String translateContext, String translateTo) {
        String thisContext;
        switch (translateContext) {
            case "a." :					thisContext = "adjective : "                   + translateTo; break;
            case "adv." :				thisContext = "adverb : "                      + translateTo; break;
            case "art." :				thisContext = "second person singular : "      + translateTo; break;
            case "art. pl." :			thisContext = "second person plural : "        + translateTo; break;
            case "conj." :				thisContext = "conjunction : "                 + translateTo; break;
            case "def." :				thisContext = "definitive : "                  + translateTo; break;
            case "excl." :				thisContext = "exclamation : "                 + translateTo; break;
            case "int." :				thisContext = "interjection : "                + translateTo; break;
            case "indef. pron." :		thisContext = "indefinite pronoun : "          + translateTo; break;
            case "int. pron." :			thisContext = "interpreted pronoun : "         + translateTo; break;
            case "inter. adv." :		thisContext = "interrogative adverb : "        + translateTo; break;
            case "l.n." :				thisContext = "literal noun : "                + translateTo; break;
            case "n." :					thisContext = "noun : "                        + translateTo; break;
            case "nom." :				thisContext = "nominal prefix : "              + translateTo; break;
            case "num." :				thisContext = "numeric : "                     + translateTo; break;
            case "part." :				thisContext = "particle : "                    + translateTo; break;
            case "pass." :				thisContext = "passive : "                     + translateTo; break;
            case "pers. pron." :		thisContext = "personal pronoun : "            + translateTo; break;
            case "pl." :				thisContext = "plural : "                      + translateTo; break;
            case "pv." :				thisContext = "passive verb : "                + translateTo; break;
            case "poss. part. pl." :	thisContext = "possessive particle, plural : " + translateTo; break;
            case "pref." :				thisContext = "prefix : "                      + translateTo; break;
            case "prep." :				thisContext = "preposition : "                 + translateTo; break;
            case "pron." :				thisContext = "pronoun : "                     + translateTo; break;
            case "pron. indef." :		thisContext = "indefinite pronoun : "          + translateTo; break;
            case "rel. pron." :			thisContext = "relative pronoun : "            + translateTo; break;
            case "sing. poss. part." :	thisContext = "single possessive particle : "  + translateTo; break;
            case "sing. poss. pron." :	thisContext = "single possessive pronoun : "   + translateTo; break;
            case "suff." :				thisContext = "suffix : "                      + translateTo; break;
            case "v." :					thisContext = "verb : "                        + translateTo; break;
            case "v.i." :				thisContext = "intransitive verb : "           + translateTo; break;
            case "v.t." :				thisContext = "transitive verb : "             + translateTo; break;
            // No editing required if none of the above situations apply
            default :					thisContext = translateContext + " : " + translateTo;
        }
        return thisContext;
    }
}