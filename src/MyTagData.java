public class MyTagData {
    public String epc;
    public String tid;
    public String user;
    
    public MyTagData(String epc, String tid, String user) {
        this.epc = epc;
        this.tid = tid;
        this.user = user;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((epc == null) ? 0 : epc.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MyTagData other = (MyTagData) obj;
        if (epc == null) {
            if (other.epc != null)
                return false;
        } else if (!epc.equals(other.epc))
            return false;
        
        return true;
    }


}
