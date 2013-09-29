# eCLJ

Short for 'embedded Clojure', eCLJ is an attempt to execute Clojure
s-expressions embedded in text, in a similar vein to Ruby's ERB.

It is still in its infancy, and as such I wouldn't recommend including
it in any production systems!

## Usage

**There is currently no deployment of eCLJ on Clojars.**

I intend to release an early version soon. At the moment, though, you
can still try eCLJ out at the REPL by cloning this repo and evaluating
the `eclj` namespace.

### Getting started

eCLJ embedded s-exprs are marked by `#=` (in a similar manner to the
'eval reader' of the normal Clojure reader):

```
;; template.eclj

I just wanted to say: #=(str "Hello" " " "World")!
```

```clojure
;; REPL:

(require '[eclj :refer [eval-eclj]])
(eval-eclj (slurp "template.eclj") {})
;; -> "I just wanted to say: Hello World!"
```

### Parameters

The main function in eCLJ is `eval-eclj`, which takes two arguments -
the template and a map of variables. The variables are then bound when
any expressions are evaluated, as follows:

```clojure
(let [text-fragment "Hello to #=(str first-name \" \" last-name)!"]
  (eval-eclj text-fragment {:first-name "Joe" :last-name "Bloggs"}))

;; -> "Hello to Joe Bloggs!"

```

### Error handling

When eCLJ encounters an error, either in parsing an expression or
evaluating the parsed expressions, it throws a [stone][1] with two
keys: `:error` and `:line`:

[1]: https://github.com/scgilardi/slingshot

```
;; template.eclj - note the typo in line 2.

#=(str first-name " " last-name) says that,
at '#=(sstr (java.util.Date.))',
it's probably time for a coffee!
```

```clojure
;; REPL:

(require '[slingshot.slingshot :refer [try+]])

(try+
  (println (eval-eclj (slurp "template.eclj")
                      {:first-name "Joe" :last-name "Bloggs"}))
					  
  (catch Object {:keys [error line] :as m}
    m))

;; -> {:line 2,
;;     :error #<CompilerException java.lang.RuntimeException:
;;              Unable to resolve symbol: sstr in this context,
;;              compiling:(NO_SOURCE_PATH:1:1)>}
```

```
;; fixed-template.eclj
#=(str first-name " " last-name) says that,
at '#=(str (java.util.Date.))',
it's probably time for a coffee!
```

```clojure
(try+
  (println (eval-eclj (slurp "fixed-template.eclj")
                      {:first-name "Joe" :last-name "Bloggs"}))
					  
  (catch Object {:keys [error line] :as m}
    m))

;; -> Joe Bloggs says that,
;;    at 'Sun Sep 29 13:39:39 BST 2013',
;;    it's probably time for a coffee!"

;; That's better!

```

### Security

**The usual rules around code injection apply here!**

Dynamically generating the templates is *probably* a bad idea.

If you *absolutely have to*, eCLJ does include a `make-safe` fn that
escapes the `#=` sequence - I'd advise wrapping any dynamically
inserted strings with this.

## Feedback

eCLJ is still very young (it began life as an 'I wonder whether this
would be possible?' at a [Likely](https://likely.co) hack day!), and
I'm not entirely sure where to take it next.

I'd be very interested to hear your thoughts!

James ([@jarohen](https://twitter.com/jarohen))

## Licence

Copyright © 2013 James Henderson

Distributed under the Eclipse Public License, the same as Clojure.
