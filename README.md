# ThunderWeasel's MTG Engine
[![CircleCI](https://circleci.com/gh/thunderweasel/magicengine.svg?style=svg)](https://circleci.com/gh/thunderweasel/magicengine)

This project should probably have a better name! I'm playing around with building a rules engine for Magic: The Gathering, mostly for fun. If you're looking for a fully functional one, look elsewhere, because this is not even close.

# Developer Guide
The project uses a pretty standard gradle setup, so it should be straight-forward to import.

## Git Hooks
There are scripts for pre-commit and pre-push hooks in the `githooks` directory that will check your code for lint issues before allowing you to commit.
It also runs tests when pushing directly to master.

If you'd like to use them, you'll first need to ensure you have [ktlint](https://github.com/pinterest/ktlint) installed. Consult the instructions there.
(Yes, it's also included via the gradle plugin, but the git hooks use the pre-installed one for speed.)

Once ktlint is installed, just run this in your project directory:
```
git config core.hooksPath githooks/
```
