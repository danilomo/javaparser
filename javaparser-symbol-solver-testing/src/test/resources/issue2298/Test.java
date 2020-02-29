package issue2298;

class Test2998 {

    public static void main(String[] args) {
        Test2998 t = new Test2998();
        int i = t.test(0, null);
    }
    
    public int test(int a, UnknownClass b) {
        return 42;
    }
    
    public int test(int a, UnknownClass b, String c) {
        return 43;
    }
    
}