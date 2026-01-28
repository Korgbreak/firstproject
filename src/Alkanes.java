//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Scanner;
//
//public class Alkanes {
//    int m;
//    boolean f;
//    int c;
//
//    Scanner scanner = new Scanner(System.in);
//    String alkan = scanner.nextLine();
//
//    int n = alkan.length();
//    char[] alkch = alkan.toCharArray();
//
//    ArrayList<String> alk = new ArrayList<>(Arrays.asList("метан", "этан", "пропан", "бутан"));
//    String metan = alk.get(0);
//    String etan = alk.get(1);
//    String propan = alk.get(2);
//    String butan = alk.get(3);
//
//    boolean find(String text){
//    char[] chrtxt = text.toCharArray();
//    m = text.length();
//    for (int i =0; i<m; ++i){
//        if (chrtxt[i] != alkch[Math.abs(n - m + i)]){
//            f = false;
//            break;
//        } else{
//            f = true;
//        }
//    }
//    return f;
//    }
//
//    boolean metan(){
//        f = find(metan);
//        return f;
//    }
//
//    boolean etan(){
//      f = find(etan);
//      return f;
//    }
//
//    boolean propan(){
//        f = find(propan);
//        return f;
//    }
//
//    boolean butan(){
//       f = find(butan);
//        return f;
//    }
//
//    int carbon(){
//        if (find(metan)){
//            c = 1;
//        }
//        if (find(etan)){
//            c = 2;
//        }
//        if (find(propan)){
//            c = 3;
//        }
//        if(find(butan)){
//            c = 4;
//        }
//        return c;
//    }
//}
