Magrit
======

Overview:
---------

Magrit wants to be a next-gen CI system. Distributed, lightweight, zero-conf, zero-infra.

The goals are:

* increase confidence of developer about their codebase
* scale team as developers join
* speed up the build, even for local-only use
* let the team decide how to use : non-intrusive system

It's using git, and only require a valid git aware system (with bash and posix tools).

zero-conf zero-infra:

* setup supposed to as simple as dowloading an installer and passing an url to it to join a project
* your computer is a node of the grid - the build system grows up with the project (you'll still be able to setup magrit on headless nodes like servers or cloud PaaS).

Use cases:
----------

__Be clean before sharing code__
This is the first step for code quality : ensure that your commit won't break the build.
You're used to it, but it's easy to miss a thing. For instance, I'm pretty sure you trigger
a build before push ; but in a DVCS world, you rarely push 1 single commit. So to be really clean, you could want to test all your commits.
So this is the first use case: trigger as many build as needed.

You now got a clean view about your changes and you decide to share thoses with a git push.

Let switch to another developer point of view. One of your buddies have push a massive bunch of commits.
And you know him so good that you're pretty sure their some dirty commit in it.

This is the second use case : __decide which 'alien' commits to integrate your local branches or not.__
With Magrit, you got a snapshot of all commits, even those that come from other developpers because
Magrit instances can share build results. Enjoy git architecture with their SHA1 commit unicity and 
you got a stable codebase.

If the build status are missing in the database (what a shame! someone pushed code without building them!!) you can still trigger builds
for new commits of the remote branch and get a good level of confidence.

Then you're ready to pick them or to leave them.
Magrit can also automatize this by choosing which commit to pick or not. Classic strategy is to pick the longest
chain of stable commits and to rebase your branch onto them. A good practice will be to trigger a fresh build
for your branch HEAD because rebasing code doesn't always work fine. (but I'm sure you know what I'm talking about ;)).

The third use case is to distribute builds among the set of developers desktop/laptop.
If you ask to build all commit for your local branch, this can envolve many, many commits (I'm talking about dozens in worst cases).
And if you build average last is minutes long, you could be waiting for hours to get a status.
With Magrit, you can share the commits (with git magic) between developers hosts, and trigger build on this instances.
By default, commits are thrown whatever happens, but you can decide to be pessimistic:
send them in logic order (from parent to the HEAD) and halt if a build fail.
This strategy will be less resource greedy and still providing a good status.

And whatever happens, if a commit is stable, it will stay stable as long as it exists.
Their is no use to build this again because SAH1 unicity ensures code unicity and build stability.
Git Magic!

Their is many other features to build on this ; if you got ideas, you can use the github bugtracker.

Screenshots:
-----------------
* Show the state of HEAD:

![magrit status](https://github.com/ptitfred/magrit/raw/resources/images/magrit-status.png)

* See the log of builds (git log like command):

![magrit build-log](https://github.com/ptitfred/magrit/raw/resources/images/magrit-build-log.png)

Participate:
----------
You can beta test this on you projects!
This is the very thing I'm currently needing.
Beside, the project isn't feature complete regarding this enthusiastic document, so be patient. Your voice can make a difference and help me prioritize open features.

This is still a incubating project, so I'm not able to let developers join for the moment.
But you can submit feature requests thru the github bugtracker or tweet me [@ptit_fred](https://twitter.com/ptit_fred).

_For french people, there is a poll to be a register beta tester (english version coming soon):_
Si vous souhaitez participer, sachez que je suis à la recherche de béta testeurs.
Pour vous manifester, vous pouvez répondre à ce formulaire :
https://docs.google.com/spreadsheet/viewform?formkey=dEV1RXp5T2M5b2VWT0ZqdFNjSzA2SWc6MQ

