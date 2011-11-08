class Env extends java.util.LinkedHashMap {
    def Outer
    Env(param=[],arg=[],outer=null) {
        this.update(this.zip(param,arg))
        Outer = outer
    } 
    def zip(params,args) {
        def dict = [:]
        params.eachWithIndex{i,j->dict[i]=args[j]}
        dict
    }
    def find(var) {
        if(this.containsKey(var)) {
            this
         } else {
             this.Outer.find(var)
         }
    }
    def update(var) {
        this.putAll(var)
    }
}
def add_globals(env) {
    def add = {a,b-> a + b}
    def sub = {a,b-> a - b}
    def mul = {a,b-> a * b}
    def div = {a,b-> a/b}
    def lt = {a,b-> a < b}
    def gt = {a,b-> a > b}
    def leq = {a,b-> a <= b}
    def geq = {a,b-> a>=b}
    def eq = {a,b-> a==b}
    env.update(['+':add,'-':sub,'*':mul,'/':div,'<':lt,'>':gt,'<=':leq,
		    '>=':geq,'==':eq])
    env
}
global_env = add_globals(new Env())

def eval(x,env=global_env) {
    if(String.isInstance(x)) {
        env.find(x)[x]
     } else if(Integer.isInstance(x)) {
        x
     } else if(x[0] == 'quote') {
        (_,exp) = x
        exp
     } else if(x[0] == 'if') {
        (_,test,conseq,alt) = x
        if(eval(test,env)) {
            eval(conseq,env)
        } else {
            eval(alt,env)
        }
     } else if(x[0] == 'set!') {
         (_,var,exp) = x
         env.find(var)[var] = eval(exp,env)
     } else if(x[0] == 'lambda') {
         (_,var,exp) = x
         assert x.size() == 3
         {args -> eval(exp,new Env(var,args,env))}
     } else if(x[0] == 'define') {
         (_,var,exp) = x
         env[var] = eval(exp,env) 
     } else {
         def exps = x.collect{exp -> eval(exp,env)}
         def proc = exps.remove(0)
         return proc(exps)
     }
}
def read_from(tokens) {
	if(tokens.size() == 0) return
	token = tokens.remove(0)
	if(token == '(') {
		def L = []
		while (tokens[0] != ')') {
			L << read_from(tokens)
		}
		tokens.remove(0)
		L
	} else if(token == ')') {
		println 'syntax error did not expect this'
	} else {
		atom(token)
	}
}

def atom(token) {
	try {
		token.toInteger();
	}catch(NumberFormatException e1) {
		try {
			token.toFloat();
		} catch(NumberFormatException e2) {
			token.toString();
		}
	}
}

def readcli() {
	def tokenise = {a -> a.replace('(',' ( ').replace(')',' ) ').tokenize()}
	System.in.eachLine{line -> print "groolisp>" ; println eval(read_from(tokenise(line)))}
}

readcli()
