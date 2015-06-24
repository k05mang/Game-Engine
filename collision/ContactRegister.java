package collision;
import java.util.ArrayList;
import java.util.HashMap;

public class ContactRegister {
	private HashMap<ContactPair, ContactPair> contacts;
	private ArrayList<ContactPair> toResolve;
	
	public ContactRegister(){
		contacts = new HashMap<ContactPair, ContactPair>();
		toResolve = new ArrayList<ContactPair>();
	}
	
	public void add(ContactPair newContact){
		if(contacts.containsKey(newContact)){
			contacts.get(newContact).addContact(newContact.getContacts().get(0));
			toResolve.add(contacts.get(newContact));
		}else{
			contacts.put(newContact, newContact);
			toResolve.add(newContact);
		}
	}
	
	public void resolve(int iterations){
		for(ContactPair pair : toResolve){
			pair.resolve(iterations);
		}
		toResolve.clear();
	}
}
