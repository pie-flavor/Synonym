# Synonym

Synonym is a Sponge command alias plugin based around [regular expression](http://en.wikipedia.org/wiki/Regular_expression). It's somewhat powerful, but may require a little to work to really start cooking. If you CBA to read the wikipedia page, click [here](https://ore.spongepowered.org/pie_flavor/Synonym/pages/Swift%20regular%20expression%20rundown).

### Config

`aliases[]`: This is essentially the root node, but it's a list to preserve order - the first to match is used.
It's a list of alias compound nodes.

Within an alias tag:

`cmd`: This is the alias which will be run. It's a regex match.

`replacement`: This is the command that the alias represents. 
It can contain capture groups (read the `\n` section [here](https://en.wikipedia.org/wiki/Regular_expression#POSIX_basic_and_extended),
but with `$` instead of '\\') from `cmd`. It can also hold variables, discussed below.

`vars{}`: This is an optional block which holds various _variables_ which are essentially a finite list of possible matchings 
for a given section. It contains variable nodes, named by how they appear in `replacement` (or even in `cmd` if you really want to do that).

Within a variable node:

`text`: This is the text that the variable's conditions will match against. This should contain at least one capture group from `cmd`.

`options[]` This is a list of option compound nodes. The order has the same meaning as alias nodes - the first to match, wins.

Within an option node:

`option`: This is regex text that is tested to match `text`. If it does, then this option is the one that is selected.

`value`: If this option is selected, then the variable will be replaced with this text.

`match`: `value` can contain capture groups. If set to `cmd` then the capture groups from `cmd` are used; if set to `option`, then the capture groups from `option` are used.

Note that any regex you see on the wiki or on the rundown page involving a backslash '\' should actually be used with two backslashes '\\' since HOCON uses backslashes too. A backslash-escaped backslash '\\' would be written as four backslashes '\\\\'.

It's a somewhat complex config to understand just from this description, so I'd recommend taking a look at the default configuration. Using it, `/gm <x>` turns into `/gamemode <x>`, and `/tell Notch <anything>` is turned into `/kill`, but leaving untouched `/tell <anyone else> <anything>.

### Changelog

1.0.0: $1

1.0.1: Fixed potential bug
