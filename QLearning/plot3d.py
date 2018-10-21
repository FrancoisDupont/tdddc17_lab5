import matplotlib as mpl
from mpl_toolkits.mplot3d import Axes3D
import numpy as np
from matplotlib import cm
import matplotlib.pyplot as plt

mpl.rcParams['legend.fontsize'] = 10

fig = plt.figure()
ax = fig.gca(projection='3d')

y = [0, 1, 2, 3, 4, 5, 6]
x = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15]
x, y = np.meshgrid(x, y)

# pour chaque x, les valeurs de la q-table (une entrée par action)
# down_left_X X --> une des valeurs (states-actions)

#down_left = [12.58441359855763, 11.880360777123112, 11.31336660958577, 13.189916008231242, 12.531330925680408]
#top_left = [19.836599767157786, 19.832697397117535, 19.835577436731924, 19.834860786798643, 19.817447906168688]
#top_right = [19.821956265511893, 19.835602414168324, 19.835192501884475, 19.833316312114228, 19.828659874817955]
#down_right = [11.693175114177784, 11.803290213087834, 11.228332689747106, 11.721193308411216, 11.60449445032267]

front_unstable = [17.179314764442758, 15.514228546987129, 15.69129981337551, 15.646329903244775, 15.523200268588859, 15.533546704395993, 15.58338981309055]
front_perfect = [96.98445715443673, 70.74948471737449, 69.53051026565137, 51.62874648322405, 49.63894900691563, 49.979666820409314, 50.93011919125416]
front_hover = [75.51267450796037, 70.55251145068897, 65.67848347489463, 62.88957822501641, 66.60263468511187, 59.642366238459836, 58.18093000027661]
front_reach_hover = [67.62591480365255, 70.47314024921683, 71.75907465251919, 71.21630792125816, 71.9605342159613, 71.84928210444066, 70.3560283744197]
right_unstable = [13.010036728141944, 12.684076110628071, 13.141880334787997, 12.90095300576903, 12.595253669281789, 13.386716895952526, 12.79204602973225]
right_perfect = [56.426192239674286, 48.2059733531389, 54.19964844537654, 28.26950983076928, 30.78225907967607, 28.4911665688116, 28.415499689485497]
right_hover = [45.23795945976569, 39.072309044782635, 61.42556545114601, 29.557888121187005, 49.156249312640846, 43.7184616559374, 39.23583132871738]
right_reach_hover = [42.27290085118914, 35.22481593929424, 46.71005791527505, 40.36990549897624, 39.06838964512598, 50.91084393416054, 35.83951137115308]
left_unstable = [13.559780984144009, 13.935456325609485, 13.193072374696541, 13.310068089688794, 12.739725425430532, 13.062821284730164, 13.37717618442741]
left_perfect = [67.26564670288977, 65.71576970741062, 54.31910288996887, 28.89056209083486, 36.03593217660048, 31.24736146023511, 28.82729118610655]
left_hover = [56.94363791096879, 57.77853967350441, 43.41039165496655, 55.27911798248156, 44.40845992231078, 46.627892525037524, 48.82873918597596]
left_reach_hover = [53.11891695856934, 43.629805498514614, 49.295447341602205, 59.33167248819494, 59.78848104361632, 52.85836581315902, 53.71330653761712]
reverse_unstable = [7.689055608868128, 7.641748408481855, 8.27445973972399, 8.742161052561894, 9.675552315995116, 9.121109634041032, 8.987142194483352]
reverse_perfect = [0, 0, 0, 0, 0, 0, 0]
reverse_hover = [0, 0, 0, 0, 0, 0, 0]
reverse_reach_hover = [2.2568681318681323, 0.6666666666666667, 0, 2.453353937728938, 0.0, 7.106408450769772, 0.0]


z = np.array([front_unstable, front_perfect, front_hover, front_reach_hover,
              right_unstable, right_perfect, right_hover, right_reach_hover,
              left_unstable, left_perfect, left_hover, left_reach_hover,
              reverse_unstable, reverse_perfect, reverse_hover, reverse_reach_hover]).T

ax.set_xlabel('\n\n\n\n\n\n\nstate')
ax.set_xticks([0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15])
#ax.set_xticklabels(['DOWN-LEFT','TOP-LEFT','TOP-RIGHT','DOWN-RIGHT'])

ax.set_ylabel('\n\n\n\n\n\n\naction')
ax.set_yticks([0, 1, 2, 3, 4, 5, 6])
#ax.set_yticklabels(['LEFT-BURN','RIGHT-BURN','MIDDLE-BURN','FULL-BURN', 'NO-BURN'])

ax.set_zlabel('q-value')
surf = ax.plot_surface(x, y, z, cmap=cm.coolwarm, linewidth=0, antialiased=False)

plt.show()