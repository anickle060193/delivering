import os
from sys import stdout
from subprocess import call
from distutils.dir_util import copy_tree


outputFolders = [ '/res/mipmap-mdpi/', '/res/mipmap-hdpi/', '/res/mipmap-xhdpi/', '/res/mipmap-xxhdpi/', '/res/mipmap-xxxhdpi/', '/' ]
outputSizes   = [ 48, 72, 96, 144, 192, 512 ]

cwd = os.getcwd()

outputResFolder = cwd + '/res'
resourceFolderLocation = cwd + '/../app/src/main/res/'

for d in outputFolders:
    if not os.path.isdir( cwd + d ):
        os.makedirs( cwd + d )

inputFile = cwd + '/ic_launcher.svg'

for folder, size in zip( outputFolders, outputSizes ):
    stdout.write( folder + '...' )
    outputFile = cwd + folder + 'ic_launcher.png'
    ret = call( [ 'inkscape.exe',
            '-f', inputFile,
            '-e', outputFile,
            '-C',
            '-w', str( size ),
            '-h', str( size ) ] )
    if ret == 0:
        print "Success"
    else:
        print "Failure"

stdout.write( '\nCopying...' )
ret = copy_tree( outputResFolder, resourceFolderLocation )
if ret:
    print "Success"
else:
    print "Failure"

raw_input( "\nPress Enter to continue..." )
