package gitlet;

/* Driver class for Gitlet, the tiny stupid version-control system.
   @author
*/
public class Main {



    /* Usage: java gitlet.Main ARGS, where ARGS contains
       <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        /*Mytils.conflictFixer("./gitlet/a.txt", "./gitlet/b.txt" , "./gitlet/a.txt");
        return;
        */
        if (args[0].equals("init")) {
            Func.init();
            return;
        }
        if (args[0].equals("add")) {
            Func.add(args[1]);
            return;
        }
        if (args[0].equals("commit")) {
            if (args[1].length() == 0) {
                System.out.println("Please enter a commit message.");
                return;
            }
            Func.commit(args[1]);
            return;
        }
        if (args[0].equals("rm")) {
            Func.remove(args[1]);
            return;
        }
        if (args[0].equals("log")) {
            Func.log();
            return;
        }
        if (args[0].equals("global-log")) {
            Func.globalLog();
            return;
        }
        if (args[0].equals("find")) {
            Func.find(args[1]);
            return;
        }
        if (args[0].equals("status")) {
            Func.status();
            return;
        }
        moreContents(args);
        return;
    }

    public static void moreContents(String... args) {
        if (args[0].equals("checkout")) {
            if (args[1].equals("--")) {
                Func.checkout1(args[2]);

            } else if (args.length == 2) {
                Func.checkout3(args[1]);
            } else if (args[2].equals("--")) {
                Func.checkout2(Mytils.path(Mytils.shortID(args[1])), args[3]);
            } else {
                System.out.println("Incorrect operands.");
            }
            return;
        }
        if (args[0].equals("branch")) {
            Func.branch(args[1]);
            return;
        }
        if (args[0].equals("rm-branch")) {
            Func.rmBranch(args[1]);
            return;
        }
        if (args[0].equals("reset")) {
            Func.reset(args[1]);
            return;
        }
        if (args[0].equals("merge")) {
            Func.merge(args[1]);
            return;
        }

    }

}

