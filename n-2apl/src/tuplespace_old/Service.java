package tuplespace_old;

import java.awt.Point;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;



import net.jini.core.discovery.LookupLocator;
import net.jini.core.entry.Entry;
import net.jini.core.entry.UnusableEntryException;
import net.jini.core.lease.Lease;
import net.jini.core.lookup.ServiceMatches;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.core.transaction.TransactionException;
import net.jini.space.JavaSpace;

public class Service {
	
	public JavaSpace space;
	
	public void addBomb(Point p) throws RemoteException, TransactionException{
		Tuple t = new Tuple("bomb", p);
		try {
			Tuple r = (Tuple) space.readIfExists(t,null,110);
			if (r == null) {
				space.write(t,null,Lease.FOREVER);
				System.out.print("bomb");
				System.out.print(" added at  ");
				System.out.println(p.toString());
			}
			else {
				System.out.print("bomb already at ");
				System.out.println(p.toString());
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnusableEntryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransactionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void removeBomb(Point p){
		Tuple t = new Tuple("bomb", p);
		Tuple r = null;
		try {
			r = (Tuple) space.takeIfExists(t,null,100);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnusableEntryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransactionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (r == null) {
			System.out.println("bomb lost");
		}
		else {
			System.out.print("bomb removed at   ");
			System.out.println(r.point.toString());
		}
	}
	
	public void writePosition(String name, Point p) throws RemoteException, TransactionException, UnusableEntryException, InterruptedException{
		Tuple old = new Tuple(name);
		Tuple read = (Tuple) space.takeIfExists(old, null, 110);
		if ( read != null) {
			System.out.print(name);
			System.out.print(" removed   ");
			System.out.println(read.point.toString());
		}
		
		Tuple t = new Tuple(name, p);

			space.write(t,null, Lease.FOREVER);
			System.out.print(name);
			System.out.print(" added at  ");
			System.out.println(p.toString());
			
			space.write(t,null, Lease.FOREVER); //TODO
	}
	
	public void initialize() throws RemoteException, UnusableEntryException, TransactionException, InterruptedException {
		System.setSecurityManager(new RMISecurityManager());
		
		
		LookupLocator ll = null;
		try {
			ll = new LookupLocator("jini://localhost:4160");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ServiceRegistrar sr = null;
		try {
			sr = ll.getRegistrar();
		} catch (IOException e) {
			
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("Service Registrar: "+sr.getServiceID());


		ServiceTemplate template = new ServiceTemplate(null, new Class[] { JavaSpace.class }, null);

		ServiceMatches sms = null;
		try {
			sms = sr.lookup(template, 10);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(0 < sms.items.length) {
		    space = (JavaSpace) sms.items[0].service;
		    System.out.println("Java Space found.");
		   
		    // do something with the space
		    Tuple t = new Tuple("test", new Point(0,0));
		    long i = 100;
		    try {
				Lease l = space.write(t, null, i);
				System.out.println(l.getExpiration());
				
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransactionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
		    System.out.println("No Java Space found.");
		}
		/*
		int i = 0;
		while (true) {
			Tuple t = new Tuple("bomb");
			Tuple r = (Tuple) space.readIfExists(t,null,0);
			if (r != null) {
				System.out.println(r.toString());
			}
			else {
				System.out.println("gone");
				break;
			}
			i++;
		}
		*/
		}
	
	}

