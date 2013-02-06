package descent.internal.compiler.parser;

import java.util.ArrayList;
import java.util.Collection;

// TODO this class should be a copy of ArrayList, and
// setDim just grows the array to dim, plus
// seting the size field to dim
public class Array<T> extends ArrayList<T> {

	private static final long serialVersionUID = 1L;
	
	public Array() {
	}
	
	public Array(int capacity) {
		super(capacity);
	}
	
	public Array(Collection<? extends T> elements) {
		super(elements);
	}
	
	public void setDim(int dim) {
		if(size() < dim)
		{
		    for(int i = size(); i < dim; i++) {
		        add(null);
		    }
		}
		else if(size() > dim)
		{
		    while(size() > dim)
		    {
		        remove(size() - 1);
		    }
		}
	}
	
	@Override
	public T set(int index, T element) {
		if (index < size()) {
			return super.set(index, element);
		} else if (index == size()) {
			add(element);
			return null;
		} else {
			for(int i = size(); i < index; i++) {
				add(i, null);
			}
			add(element);
			return null;
		}
	}
	
	public void zero() {
		for(int i = 0; i < size(); i++) {
			set(i, null);
		}
	}
	
	public void memcpy(Array<? extends T> other) {
		if (other == null) {
			return;
		}
		
		for(int i = 0; i < other.size(); i++) {
			set(i, other.get(i));
		}
	}
	
	public void shift(T obj) {
		this.add(0, obj);
	}

}
