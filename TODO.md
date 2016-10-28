TODO: Roadmap to features & functionality
========================================================================

## 0.0.0 

UI allowing users to

- create expenses 
- create groups 
- add/switch expenses to groups
- list a week's expenses + summary
- import BGI format data job/method
- Send email of expenses listed by group each week

## 0.1.0

UI allowing uses to 

- create actions
- able to define simple formulas for how much expenses cost in actions
- invoice user based on actions/expenses

## 0.1.1

Core functionality done by this point, work on features:

- automating lets-encrypt + SSL based on configured domain
- setup basic authentication filter once SSL done

## 0.1.2

- email the system to add expense?
- more advanced billing system formulas

[Handy post]:http://manuel.bernhardt.io/2014/04/23/a-handful-akka-techniques/

### Minor notes to self:

- Need to deal with shutting down mysql connections on app end
	- Check `show processlist`, no threads
	- `~run`, check `show processlist`, see threads
	- `enter` app stops running, check `show processlist` should not see threads but do
