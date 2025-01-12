Ex2

This project is a simple spreadsheet application. It allows users to create, edit, and evaluate cells in a 2D spreadsheet. 
Cells can contain numbers, text, or formulas. The spreadsheet supports saving and loading data to/from a text file. 
On top of adding formulas and so, the application can also detect various kinds of different errors, from a formula that has its parentheses flipped to a cells
which formula creates a circular error with itelf or a different cell. 

Interfaces:
Index2D--
meant represent the placement of a cell in a 2d sheet
Cell--
meant to represent a singular cell and its attributes
sheet--
meant to represent the spreadsheet of all the cells

Classes:

CellEntry--
a class meant to represent the placement of a cell in a 2D spreadsheet of cells.
implements Index2D
SCell--
 implements Cell , meant to represent a singular cell and adds extra methods for calculation whthin the cell.
 Ex2Sheet--
 implements sheet Interface
 Ex2GUI--
 Main class for graphic user Interface
 Ex2Utils--
 Utilty class meant to save static final objects used throught the whole application.
 StdDrawEx2--
 Library for initializing the GUI.

 ![תמונה של WhatsApp‏ 2025-01-12 בשעה 08 28 56_668b5492](https://github.com/user-attachments/assets/407bfac9-8115-411b-8d1d-08e787475d2e)
